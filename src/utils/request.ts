interface RequestConfig {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: any;
  header?: Record<string, string>;
  timeout?: number;
}

interface ApiResponse<T = any> {
  data: T;
  statusCode: number;
  header: Record<string, string>;
}

class ApiClient {
  private baseURL: string;
  private timeout: number;
  private defaultHeaders: Record<string, string>;

  constructor() {
    // 开发环境配置 - 使用相对路径，由Vite代理到后端
    this.baseURL = '/api';
    this.timeout = 10000;
    this.defaultHeaders = {
      'Content-Type': 'application/json'
    };
  }

  // 请求拦截器
  private beforeRequest(config: RequestConfig): RequestConfig {
    // 设置headers支持session
    config.header = {
      ...config.header,
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    };

    // 添加认证token (如果后端支持token)
    const token = uni.getStorageSync('token');
    if (token) {
      config.header['Authorization'] = `Bearer ${token}`;
    }

    // 日志记录
    console.log('[API请求]', config.method || 'GET', config.url, config.data);
    
    return config;
  }

  // 响应拦截器
  private afterResponse<T>(response: UniApp.RequestSuccessCallbackResult): ApiResponse<T> {
    const { statusCode, data, header } = response;

    console.log('[API响应]', statusCode, data);

    // 状态码检查
    if (statusCode >= 200 && statusCode < 300) {
      return { data: data as T, statusCode, header };
    } else {
      // 更详细的错误信息
      const errorData = data as any;
      const errorMessage = errorData?.message || errorData?.detail || errorData?.error || `HTTP Error: ${statusCode}`;
      const error = new Error(errorMessage);
      (error as any).data = errorData;
      (error as any).statusCode = statusCode;
      throw error;
    }
  }

  // 通用请求方法
  async request<T = any>(config: RequestConfig): Promise<T> {
    return new Promise((resolve, reject) => {
      // 应用请求拦截器
      const processedConfig = this.beforeRequest(config);
      
      // 完整URL
      const fullUrl = config.url.startsWith('http') 
        ? config.url 
        : `${this.baseURL}${config.url}`;

      uni.request({
        url: fullUrl,
        method: processedConfig.method || 'GET',
        data: processedConfig.data,
        header: {
          ...this.defaultHeaders,
          ...processedConfig.header
        },
        timeout: this.timeout,
        withCredentials: true, // 支持携带cookies
        success: (response) => {
          try {
            const processedResponse = this.afterResponse<T>(response);
            resolve(processedResponse.data);
          } catch (error) {
            reject(error);
          }
        },
        fail: (error) => {
          console.error('[API请求失败]', error);
          reject(new Error(error.errMsg || '网络请求失败'));
        }
      });
    });
  }

  // GET请求
  async get<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'GET',
      data
    });
  }

  // POST请求
  async post<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'POST',
      data
    });
  }

  // PUT请求
  async put<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'PUT',
      data
    });
  }

  // DELETE请求
  async delete<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'DELETE',
      data
    });
  }
}

// 创建实例
const apiClient = new ApiClient();

export default apiClient;