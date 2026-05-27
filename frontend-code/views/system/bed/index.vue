<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="???? ID" prop="orgId">
        <el-input
          v-model="queryParams.orgId"
          placeholder="请输入???? ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="?????" prop="bedCount">
        <el-input
          v-model="queryParams.bedCount"
          placeholder="请输入?????"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="?????" prop="actualBedCount">
        <el-input
          v-model="queryParams.actualBedCount"
          placeholder="请输入?????"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="?????" prop="usedBedCount">
        <el-input
          v-model="queryParams.usedBedCount"
          placeholder="请输入?????"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="??????%%?" prop="bedUsageRate">
        <el-input
          v-model="queryParams.bedUsageRate"
          placeholder="请输入??????%%?"
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
      <el-form-item label="????" prop="statMonth">
        <el-input
          v-model="queryParams.statMonth"
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
      <el-form-item label="${comment}" prop="updatedAt">
        <el-date-picker clearable
          v-model="queryParams.updatedAt"
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
          v-hasPermi="['system:bed:add']"
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
          v-hasPermi="['system:bed:edit']"
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
          v-hasPermi="['system:bed:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:bed:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bedList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="??" align="center" prop="id" />
      <el-table-column label="???? ID" align="center" prop="orgId" />
      <el-table-column label="?????" align="center" prop="bedCount" />
      <el-table-column label="?????" align="center" prop="actualBedCount" />
      <el-table-column label="?????" align="center" prop="usedBedCount" />
      <el-table-column label="??????%%?" align="center" prop="bedUsageRate" />
      <el-table-column label="????" align="center" prop="deptType" />
      <el-table-column label="????" align="center" prop="statYear" />
      <el-table-column label="????" align="center" prop="statMonth" />
      <el-table-column label="${comment}" align="center" prop="createdAt" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="${comment}" align="center" prop="updatedAt" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updatedAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:bed:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:bed:remove']"
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

    <!-- 添加或修改???????对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="???? ID" prop="orgId">
              <el-input v-model="form.orgId" placeholder="请输入???? ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="?????" prop="bedCount">
              <el-input v-model="form.bedCount" placeholder="请输入?????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="?????" prop="actualBedCount">
              <el-input v-model="form.actualBedCount" placeholder="请输入?????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="?????" prop="usedBedCount">
              <el-input v-model="form.usedBedCount" placeholder="请输入?????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="??????%%?" prop="bedUsageRate">
              <el-input v-model="form.bedUsageRate" placeholder="请输入??????%%?" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="????" prop="statYear">
              <el-input v-model="form.statYear" placeholder="请输入????" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="????" prop="statMonth">
              <el-input v-model="form.statMonth" placeholder="请输入????" />
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
          <el-col :span="24">
            <el-form-item label="${comment}" prop="updatedAt">
              <el-date-picker clearable
                v-model="form.updatedAt"
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
import { listBed, getBed, delBed, addBed, updateBed } from "@/api/system/bed"

export default {
  name: "Bed",
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
      // ???????表格数据
      bedList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orgId: null,
        bedCount: null,
        actualBedCount: null,
        usedBedCount: null,
        bedUsageRate: null,
        deptType: null,
        statYear: null,
        statMonth: null,
        createdAt: null,
        updatedAt: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        orgId: [
          { required: true, message: "???? ID不能为空", trigger: "blur" }
        ],
        bedCount: [
          { required: true, message: "?????不能为空", trigger: "blur" }
        ],
        actualBedCount: [
          { required: true, message: "?????不能为空", trigger: "blur" }
        ],
        usedBedCount: [
          { required: true, message: "?????不能为空", trigger: "blur" }
        ],
        statYear: [
          { required: true, message: "????不能为空", trigger: "blur" }
        ],
        createdAt: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        updatedAt: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询???????列表 */
    getList() {
      this.loading = true
      listBed(this.queryParams).then(response => {
        this.bedList = response.rows
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
        orgId: null,
        bedCount: null,
        actualBedCount: null,
        usedBedCount: null,
        bedUsageRate: null,
        deptType: null,
        statYear: null,
        statMonth: null,
        createdAt: null,
        updatedAt: null
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
      this.title = "添加???????"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getBed(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改???????"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateBed(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addBed(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除???????编号为"' + ids + '"的数据项？').then(function() {
        return delBed(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/bed/export', {
        ...this.queryParams
      }, `bed_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
