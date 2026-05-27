<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="?? ID" prop="regionId">
        <el-input
          v-model="queryParams.regionId"
          placeholder="请输入?? ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="???" prop="totalPopulation">
        <el-input
          v-model="queryParams.totalPopulation"
          placeholder="请输入???"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="????" prop="malePopulation">
        <el-input
          v-model="queryParams.malePopulation"
          placeholder="请输入????"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="????" prop="femalePopulation">
        <el-input
          v-model="queryParams.femalePopulation"
          placeholder="请输入????"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="0-14???" prop="age014">
        <el-input
          v-model="queryParams.age014"
          placeholder="请输入0-14???"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="15-59???" prop="age1559">
        <el-input
          v-model="queryParams.age1559"
          placeholder="请输入15-59???"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="60?????" prop="age60Plus">
        <el-input
          v-model="queryParams.age60Plus"
          placeholder="请输入60?????"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="????" prop="statYear">
        <el-input
          v-model="queryParams.statYear"
          placeholder="请输入????"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="createdAt">
        <el-date-picker clearable
          v-model="queryParams.createdAt"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择${comment}">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:population:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:population:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:population:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:population:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="populationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="${comment}" align="center" prop="id" />
      <el-table-column label="?? ID" align="center" prop="regionId" />
      <el-table-column label="???" align="center" prop="totalPopulation" />
      <el-table-column label="????" align="center" prop="malePopulation" />
      <el-table-column label="????" align="center" prop="femalePopulation" />
      <el-table-column label="0-14???" align="center" prop="age014" />
      <el-table-column label="15-59???" align="center" prop="age1559" />
      <el-table-column label="60?????" align="center" prop="age60Plus" />
      <el-table-column label="????" align="center" prop="statYear" />
      <el-table-column label="${comment}" align="center" prop="createdAt" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:population:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:population:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改?????对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="?? ID" prop="regionId">
              <el-input v-model="form.regionId" placeholder="请输入?? ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="???" prop="totalPopulation">
              <el-input v-model="form.totalPopulation" placeholder="请输入???" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="????" prop="malePopulation">
              <el-input v-model="form.malePopulation" placeholder="请输入????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="????" prop="femalePopulation">
              <el-input v-model="form.femalePopulation" placeholder="请输入????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="0-14???" prop="age014">
              <el-input v-model="form.age014" placeholder="请输入0-14???" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="15-59???" prop="age1559">
              <el-input v-model="form.age1559" placeholder="请输入15-59???" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="60?????" prop="age60Plus">
              <el-input v-model="form.age60Plus" placeholder="请输入60?????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="????" prop="statYear">
              <el-input v-model="form.statYear" placeholder="请输入????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="${comment}" prop="createdAt">
              <el-date-picker clearable
                v-model="form.createdAt"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择${comment}">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPopulation, getPopulation, delPopulation, addPopulation, updatePopulation } from "@/api/system/population"

export default {
  name: "Population",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // ?????表格数据
      populationList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        regionId: null,
        totalPopulation: null,
        malePopulation: null,
        femalePopulation: null,
        age014: null,
        age1559: null,
        age60Plus: null,
        statYear: null,
        createdAt: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        regionId: [
          { required: true, message: "?? ID不能为空", trigger: "blur" }
        ],
        totalPopulation: [
          { required: true, message: "???不能为空", trigger: "blur" }
        ],
        statYear: [
          { required: true, message: "????不能为空", trigger: "blur" }
        ],
        createdAt: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询?????列表 */
    getList() {
      this.loading = true
      listPopulation(this.queryParams).then(response => {
        this.populationList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        regionId: null,
        totalPopulation: null,
        malePopulation: null,
        femalePopulation: null,
        age014: null,
        age1559: null,
        age60Plus: null,
        statYear: null,
        createdAt: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加?????"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getPopulation(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改?????"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updatePopulation(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addPopulation(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除?????编号为"' + ids + '"的数据项？').then(function() {
        return delPopulation(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/population/export', {
        ...this.queryParams
      }, `population_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
