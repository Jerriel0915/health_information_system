import os

class Settings:
    # DashScope (ASR + TTS) — 通过环境变量设置
    dashscope_api_key: str = os.getenv("DASHSCOPE_API_KEY", "")
    
    # Qwen (LLM) — 用同一个 Key
    qwen_api_key: str = os.getenv("DASHSCOPE_API_KEY", "")
    qwen_base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    qwen_model: str = "qwen3.6-flash"
    
    # ASR
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
