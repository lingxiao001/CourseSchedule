module.exports = {
    globals: {
        uni: true,
        wx: true,
        $3:  'readonly',
    },
    root: true,
    env: {
      browser: true,
      node: true,
      es2021: true,
    },
    // 用 vue-eslint-parser 先做外层解析
    parser: 'vue-eslint-parser',
    parserOptions: {
      // 交给 @babel/eslint-parser 真正解析 JS
      parser: '@babel/eslint-parser',
      requireConfigFile: false,   // 不强制 babel 要有 .babelrc
      ecmaVersion: 2021,
      sourceType: 'module',
      ecmaFeatures: {
        jsx: false,
      },
    },
    extends: [
      'plugin:vue/vue3-essential',  // 或 vue3-recommended / vue3-strongly-recommended
      'eslint:recommended'
    ],
    // 只 lint src 里的文件，跳过配置、构建文件
    ignorePatterns: [
      'node_modules/',
      'dist/',
      'vite.config.js',
      'vue.config.js'
    ],
    rules: {
      // 你的自定义规则……
      // 'no-console': 'warn',
      // 'vue/no-unused-components': 'warn',
      'no-unused-vars': 'off',
    }
  };
  