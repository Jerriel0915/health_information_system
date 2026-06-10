import os

class Settings:
    # DashScope (ASR + TTS) 鈥?閫氳繃鐜鍙橀噺璁剧疆
    dashscope_api_key: str = os.getenv("DASHSCOPE_API_KEY", "")
    
    # Qwen (LLM) 鈥?鐢ㄥ悓涓€涓?Key
    qwen_api_key: str = os.getenv("DASHSCOPE_API_KEY", "")
    qwen_base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    qwen_model: str = "qwen3.6-flash"
    
    # ASR — L1 修复：
    #   Python 端和 Java 端现在统一使用 paraformer-realtime-v2
    #   历史：Java 端曾用 paraformer-v2（非流式），后改为 realtime 统一
    #   精度相当，realtime 版支持短语音流式输入，体验更一致
    asr_model: str = "paraformer-realtime-v2"
    
    # TTS
    tts_model: str = "sambert-zhiyuan-v1"
    tts_sample_rate: int = 48000
    tts_format: str = "wav"
    
    # Database
    db_host: str = "localhost"
    db_port: int = 3306
    db_name: str = "health_data_manager"
    db_user: str = "root"
    db_pass: str = "l123456789"

settings = Settings()
