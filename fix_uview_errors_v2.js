#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

// 修复规则
const fixRules = [
  // 修复标签闭合错误 - 更精确的规则
  {
    pattern: /<text([1-4])([^>]*)>([^<]*)<\/text>/g,
    replacement: '<text$1$2>$3</text$1>'
  },
  {
    pattern: /<p([^>]*)>([^<]*)<\/text>/g,
    replacement: '<text$1>$2</text>'
  },
  {
    pattern: /<p([^>]*)>([^<]*)<\/p>/g,
    replacement: '<text$1>$2</text>'
  },
  {
    pattern: /<h([1-6])([^>]*)>([^<]*)<\/text>/g,
    replacement: '<text$1$2>$3</text$1>'
  },
  {
    pattern: /<h([1-6])([^>]*)>([^<]*)<\/h([1-6])>/g,
    replacement: '<text$1$2>$3</text$1>'
  },
  
  // 修复 u-input-number 组件
  {
    pattern: /<u-input-number([^>]*)><\/u-number-box>/g,
    replacement: '<u-number-box$1></u-number-box>'
  },
  
  // 修复属性语法错误
  {
    pattern: /::clearable="true"="true"/g,
    replacement: ':clearable="true"'
  },
  {
    pattern: /::border="true"="true"/g,
    replacement: ':border="true"'
  },
  {
    pattern: /::stripe="true"="true"/g,
    replacement: ':stripe="true"'
  },
  
  // 修复 CSS 中的错误
  {
    pattern: /:border="true"-radius/g,
    replacement: 'border-radius'
  },
  {
    pattern: /:border="true"-bottom/g,
    replacement: 'border-bottom'
  },
  {
    pattern: /:border="true"-top/g,
    replacement: 'border-top'
  }
];

// 移除重复的 uni 定义
const removeDuplicateUni = (content) => {
  // 移除所有 uni 定义块
  content = content.replace(/\/\/ #ifdef H5[\s\S]*?\/\/ #endif/g, '');
  content = content.replace(/\/\/ #ifndef H5[\s\S]*?\/\/ #endif/g, '');
  
  // 移除单独的 uni 定义
  content = content.replace(/const uni = \{[\s\S]*?\};/g, '');
  
  return content;
};

// 移除未使用的导入
const removeUnusedImports = (content) => {
  // 移除 ElMessage 相关导入
  content = content.replace(/import\s*{\s*ElMessage[^}]*}\s*from\s*['"]element-plus['"];?\s*/g, '');
  content = content.replace(/import\s*{\s*ElMessageBox[^}]*}\s*from\s*['"]element-plus['"];?\s*/g, '');
  
  // 移除 Element Plus 图标导入
  content = content.replace(/import\s*{\s*[^}]*}\s*from\s*['"]@element-plus\/icons-vue['"];?\s*/g, '');
  
  return content;
};

// 添加全局 uni 定义（只在 main.js 中添加一次）
const addGlobalUni = (content) => {
  if (content.includes('uni.') && !content.includes('// #ifdef')) {
    const uniDefinition = `
// 全局 uni 对象定义
const uni = {
  showToast: (options) => {
    if (options.icon === 'success') {
      alert('✅ ' + options.title);
    } else if (options.icon === 'error') {
      alert('❌ ' + options.title);
    } else {
      alert(options.title);
    }
  },
  showModal: (options) => {
    const result = confirm(options.content || options.title);
    if (options.success) {
      options.success({ confirm: result });
    }
  },
  navigateTo: (options) => {
    window.location.href = options.url;
  },
  navigateBack: () => {
    window.history.back();
  },
  redirectTo: (options) => {
    window.location.replace(options.url);
  },
  reLaunch: (options) => {
    window.location.href = options.url;
  }
};
`;
    
    // 在 script 标签开始后添加
    content = content.replace(/(<script[^>]*>)/, `$1\n${uniDefinition}`);
  }
  
  return content;
};

function fixFile(filePath) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    let originalContent = content;
    
    // 移除重复的 uni 定义
    content = removeDuplicateUni(content);
    
    // 应用修复规则
    for (const rule of fixRules) {
      content = content.replace(rule.pattern, rule.replacement);
    }
    
    // 移除未使用的导入
    content = removeUnusedImports(content);
    
    // 添加全局 uni 定义
    content = addGlobalUni(content);
    
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8');
      console.log(`✅ 已修复: ${filePath}`);
      return true;
    } else {
      console.log(`⏭️  无需修复: ${filePath}`);
      return false;
    }
  } catch (error) {
    console.error(`❌ 修复失败: ${filePath}`, error.message);
    return false;
  }
}

function processDirectory(dirPath) {
  const files = fs.readdirSync(dirPath);
  let fixedCount = 0;
  
  for (const file of files) {
    const filePath = path.join(dirPath, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      fixedCount += processDirectory(filePath);
    } else if (file.endsWith('.vue')) {
      if (fixFile(filePath)) {
        fixedCount++;
      }
    }
  }
  
  return fixedCount;
}

function main() {
  const args = process.argv.slice(2);
  const targetPath = args[0] || './vue01/src';
  
  if (!fs.existsSync(targetPath)) {
    console.error(`❌ 路径不存在: ${targetPath}`);
    process.exit(1);
  }
  
  console.log('🔧 开始修复 uView 替换错误 (v2)...');
  console.log(`📁 目标路径: ${targetPath}`);
  
  const fixedCount = processDirectory(targetPath);
  
  console.log(`\n🎉 修复完成！共修复了 ${fixedCount} 个文件`);
  console.log('\n📝 修复内容:');
  console.log('1. 移除了重复的 uni 定义');
  console.log('2. 修复了标签闭合错误');
  console.log('3. 修复了组件标签错误');
  console.log('4. 修复了属性语法错误');
  console.log('5. 移除了未使用的导入');
  console.log('6. 添加了全局 uni 定义');
}

if (require.main === module) {
  main();
}

module.exports = {
  fixFile,
  processDirectory,
  fixRules,
  removeDuplicateUni,
  removeUnusedImports,
  addGlobalUni
}; 