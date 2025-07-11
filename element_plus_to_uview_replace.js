#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

// Element Plus 到 uView UI 的组件映射
const componentMapping = {
  // 基础组件
  'el-button': 'u-button',
  'el-input': 'u-input',
  'el-select': 'u-select',
  'el-option': 'u-option',
  'el-form': 'u-form',
  'el-form-item': 'u-form-item',
  'el-table': 'u-table',
  'el-table-column': 'u-table-column',
  'el-dialog': 'u-popup',
  'el-card': 'u-card',
  'el-tag': 'u-tag',
  'el-pagination': 'u-pagination',
  'el-input-number': 'u-number-box',
  'el-divider': 'u-line',
  'el-page-header': 'u-navbar',
  'el-icon': 'u-icon',
  'el-empty': 'u-empty',
  'el-spinner': 'u-loading',
  'el-popover': 'u-popover',
  'el-tooltip': 'u-tooltip',
  'el-dropdown': 'u-dropdown',
  'el-menu': 'u-menu',
  'el-menu-item': 'u-menu-item',
  'el-submenu': 'u-submenu',
  'el-breadcrumb': 'u-breadcrumb',
  'el-breadcrumb-item': 'u-breadcrumb-item',
  'el-steps': 'u-steps',
  'el-step': 'u-step',
  'el-tabs': 'u-tabs',
  'el-tab-pane': 'u-tab-pane',
  'el-collapse': 'u-collapse',
  'el-collapse-item': 'u-collapse-item',
  'el-tree': 'u-tree',
  'el-upload': 'u-upload',
  'el-rate': 'u-rate',
  'el-slider': 'u-slider',
  'el-switch': 'u-switch',
  'el-checkbox': 'u-checkbox',
  'el-radio': 'u-radio',
  'el-radio-group': 'u-radio-group',
  'el-checkbox-group': 'u-checkbox-group',
  'el-date-picker': 'u-datetime-picker',
  'el-time-picker': 'u-time-picker',
  'el-color-picker': 'u-color-picker',
  'el-cascader': 'u-cascader',
  'el-transfer': 'u-transfer',
  'el-progress': 'u-progress',
  'el-badge': 'u-badge',
  'el-avatar': 'u-avatar',
  'el-skeleton': 'u-skeleton',
  'el-alert': 'u-alert',
  'el-message': 'u-message',
  'el-notification': 'u-notification',
  'el-loading': 'u-loading',
  'el-infinite-scroll': 'u-infinite-scroll',
  'el-backtop': 'u-back-top',
  'el-drawer': 'u-drawer',
  'el-autocomplete': 'u-autocomplete',
  'el-cascader-panel': 'u-cascader-panel',
  'el-checkbox-button': 'u-checkbox-button',
  'el-radio-button': 'u-radio-button',
  'el-tag': 'u-tag',
  'el-link': 'u-link',
  'el-image': 'u-image',
  'el-calendar': 'u-calendar',
  'el-descriptions': 'u-descriptions',
  'el-descriptions-item': 'u-descriptions-item',
  'el-result': 'u-result',
  'el-timeline': 'u-timeline',
  'el-timeline-item': 'u-timeline-item',
  'el-divider': 'u-line',
  'el-affix': 'u-affix',
  'el-anchor': 'u-anchor',
  'el-anchor-link': 'u-anchor-link',
  'el-backtop': 'u-back-top',
  'el-page-header': 'u-navbar',
  'el-carousel': 'u-swiper',
  'el-carousel-item': 'u-swiper-item',
  'el-collapse': 'u-collapse',
  'el-collapse-item': 'u-collapse-item',
  'el-color-picker': 'u-color-picker',
  'el-container': 'u-container',
  'el-header': 'u-header',
  'el-aside': 'u-aside',
  'el-main': 'u-main',
  'el-footer': 'u-footer',
  'el-row': 'u-row',
  'el-col': 'u-col',
  'el-space': 'u-space',
  'el-scrollbar': 'u-scroll-view',
  'el-text': 'u-text',
  'el-paragraph': 'u-paragraph',
  'el-title': 'u-title',
  'el-blockquote': 'u-blockquote',
  'el-ol': 'u-ol',
  'el-ul': 'u-ul',
  'el-li': 'u-li',
  'el-code': 'u-code',
  'el-pre': 'u-pre',
  'el-kbd': 'u-kbd',
  'el-strong': 'u-strong',
  'el-em': 'u-em',
  'el-del': 'u-del',
  'el-ins': 'u-ins',
  'el-mark': 'u-mark',
  'el-time': 'u-time',
  'el-abbr': 'u-abbr',
  'el-address': 'u-address',
  'el-cite': 'u-cite',
  'el-q': 'u-q',
  'el-samp': 'u-samp',
  'el-var': 'u-var',
  'el-b': 'u-b',
  'el-i': 'u-i',
  'el-s': 'u-s',
  'el-u': 'u-u',
  'el-small': 'u-small',
  'el-big': 'u-big',
  'el-sub': 'u-sub',
  'el-sup': 'u-sup',
  'el-tt': 'u-tt',
  'el-strike': 'u-strike',
  'el-blink': 'u-blink',
  'el-marquee': 'u-marquee',
  'el-multicol': 'u-multicol',
  'el-listing': 'u-listing',
  'el-xmp': 'u-xmp',
  'el-plaintext': 'u-plaintext',
  'el-listing': 'u-listing',
  'el-xmp': 'u-xmp',
  'el-plaintext': 'u-plaintext',
  'el-listing': 'u-listing',
  'el-xmp': 'u-xmp',
  'el-plaintext': 'u-plaintext'
};

// 属性映射
const attributeMapping = {
  // 按钮属性
  'type="danger"': 'type="error"',
  'size="small"': 'size="mini"',
  'size="large"': 'size="large"',
  
  // 输入框属性
  'clearable': ':clearable="true"',
  '@keyup.enter': '@confirm',
  
  // 表格属性
  'border': ':border="true"',
  'stripe': ':stripe="true"',
  'v-loading': ':loading',
  
  // 弹窗属性
  'width="600px"': ':width="600"',
  '@closed': '@close',
  
  // 图标属性
  ':icon="Plus"': ':icon="\'plus\'"',
  ':icon="Search"': ':icon="\'search\'"',
  ':icon="ArrowLeftBold"': ':icon="\'arrow-left\'"',
  ':icon="Edit"': ':icon="\'edit-pen\'"',
  ':icon="Delete"': ':icon="\'trash\'"',
  ':icon="Close"': ':icon="\'close\'"',
  ':icon="Check"': ':icon="\'checkmark\'"',
  ':icon="Warning"': ':icon="\'warning\'"',
  ':icon="Info"': ':icon="\'info-circle\'"',
  ':icon="Success"': ':icon="\'checkmark-circle\'"',
  ':icon="Error"': ':icon="\'close-circle\'"',
  
  // 模板插槽
  '#append': '#suffix',
  '#default="scope"': '#default="{ row }"',
  
  // 标签替换
  '<div': '<view',
  '</div>': '</view>',
  '<span': '<text',
  '</span>': '</text>',
  '<p>': '<text>',
  '</p>': '</text>'
};

// 图标映射
const iconMapping = {
  'Plus': 'plus',
  'Search': 'search',
  'ArrowLeftBold': 'arrow-left',
  'Edit': 'edit-pen',
  'Delete': 'trash',
  'Close': 'close',
  'Check': 'checkmark',
  'Warning': 'warning',
  'Info': 'info-circle',
  'Success': 'checkmark-circle',
  'Error': 'close-circle',
  'User': 'account',
  'Setting': 'setting',
  'Home': 'home',
  'Menu': 'menu',
  'Refresh': 'reload',
  'Download': 'download',
  'Upload': 'upload',
  'Share': 'share',
  'Like': 'heart',
  'Star': 'star',
  'Eye': 'eye',
  'EyeOff': 'eye-off',
  'Lock': 'lock',
  'Unlock': 'unlock',
  'Key': 'key',
  'Mail': 'email',
  'Phone': 'phone',
  'Location': 'map-pin',
  'Calendar': 'calendar',
  'Clock': 'clock',
  'Camera': 'camera',
  'Image': 'image',
  'File': 'file',
  'Folder': 'folder',
  'Link': 'link',
  'External': 'external-link',
  'ArrowUp': 'arrow-up',
  'ArrowDown': 'arrow-down',
  'ArrowRight': 'arrow-right',
  'ArrowLeft': 'arrow-left',
  'ChevronUp': 'chevron-up',
  'ChevronDown': 'chevron-down',
  'ChevronRight': 'chevron-right',
  'ChevronLeft': 'chevron-left',
  'Minus': 'minus',
  'Plus': 'plus',
  'X': 'close',
  'Check': 'checkmark',
  'Alert': 'alert-circle',
  'Help': 'help-circle',
  'Info': 'info-circle',
  'XCircle': 'close-circle',
  'CheckCircle': 'checkmark-circle',
  'AlertTriangle': 'alert-triangle',
  'AlertOctagon': 'alert-octagon',
  'AlertSquare': 'alert-square',
  'AlertCircle': 'alert-circle',
  'AlertHexagon': 'alert-hexagon',
  'AlertDiamond': 'alert-diamond',
  'AlertTriangle': 'alert-triangle',
  'AlertSquare': 'alert-square',
  'AlertCircle': 'alert-circle',
  'AlertHexagon': 'alert-hexagon',
  'AlertDiamond': 'alert-diamond'
};

// 消息提示映射
const messageMapping = {
  'ElMessage.success': 'uni.showToast({ title: \'$1\', icon: \'success\' })',
  'ElMessage.error': 'uni.showToast({ title: \'$1\', icon: \'error\' })',
  'ElMessage.warning': 'uni.showToast({ title: \'$1\', icon: \'none\' })',
  'ElMessage.info': 'uni.showToast({ title: \'$1\', icon: \'none\' })',
  'ElMessageBox.confirm': 'uni.showModal({ title: \'$1\', content: \'$2\', success: (res) => { if (res.confirm) { $3 } } })',
  'ElMessageBox.alert': 'uni.showModal({ title: \'$1\', content: \'$2\', showCancel: false })',
  'ElMessageBox.prompt': 'uni.showModal({ title: \'$1\', content: \'$2\', editable: true, success: (res) => { if (res.confirm) { $3 } } })'
};

// 路由映射
const routerMapping = {
  'this.$router.push': 'uni.navigateTo',
  'this.$router.replace': 'uni.redirectTo',
  'this.$router.go(-1)': 'uni.navigateBack()',
  'this.$router.back()': 'uni.navigateBack()',
  'this.$route.params': 'onLoad(options)',
  'this.$route.query': 'onLoad(options)'
};

function replaceComponents(content) {
  let result = content;
  
  // 替换组件标签
  for (const [oldComponent, newComponent] of Object.entries(componentMapping)) {
    const regex = new RegExp(`<${oldComponent}([^>]*)>`, 'g');
    result = result.replace(regex, `<${newComponent}$1>`);
    
    const closeRegex = new RegExp(`</${oldComponent}>`, 'g');
    result = result.replace(closeRegex, `</${newComponent}>`);
  }
  
  // 替换属性
  for (const [oldAttr, newAttr] of Object.entries(attributeMapping)) {
    const regex = new RegExp(oldAttr, 'g');
    result = result.replace(regex, newAttr);
  }
  
  // 替换图标
  for (const [oldIcon, newIcon] of Object.entries(iconMapping)) {
    const regex = new RegExp(`:icon="${oldIcon}"`, 'g');
    result = result.replace(regex, `:icon="'${newIcon}'"`);
    
    const iconRegex = new RegExp(`<el-icon><${oldIcon} /></el-icon>`, 'g');
    result = result.replace(iconRegex, `<u-icon name="${newIcon}" />`);
  }
  
  // 替换消息提示
  for (const [oldMessage, newMessage] of Object.entries(messageMapping)) {
    const regex = new RegExp(oldMessage.replace('$1', '([^)]+)').replace('$2', '([^)]+)').replace('$3', '([^)]+)'), 'g');
    result = result.replace(regex, newMessage);
  }
  
  // 替换路由
  for (const [oldRouter, newRouter] of Object.entries(routerMapping)) {
    const regex = new RegExp(oldRouter, 'g');
    result = result.replace(regex, newRouter);
  }
  
  return result;
}

function processFile(filePath) {
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    const newContent = replaceComponents(content);
    
    if (content !== newContent) {
      fs.writeFileSync(filePath, newContent, 'utf8');
      console.log(`✅ 已处理: ${filePath}`);
      return true;
    } else {
      console.log(`⏭️  无需处理: ${filePath}`);
      return false;
    }
  } catch (error) {
    console.error(`❌ 处理失败: ${filePath}`, error.message);
    return false;
  }
}

function processDirectory(dirPath) {
  const files = fs.readdirSync(dirPath);
  let processedCount = 0;
  
  for (const file of files) {
    const filePath = path.join(dirPath, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      processedCount += processDirectory(filePath);
    } else if (file.endsWith('.vue') || file.endsWith('.js')) {
      if (processFile(filePath)) {
        processedCount++;
      }
    }
  }
  
  return processedCount;
}

function updateMainJs(mainJsPath) {
  try {
    let content = fs.readFileSync(mainJsPath, 'utf8');
    
    // 移除 Element Plus 相关导入
    content = content.replace(/import ElementPlus from 'element-plus'/g, '');
    content = content.replace(/import 'element-plus\/dist\/index\.css'/g, '');
    content = content.replace(/import \* as ElementPlusIconsVue from '@element-plus\/icons-vue'/g, '');
    
    // 移除 Element Plus 注册
    content = content.replace(/app\.use\(ElementPlus\)/g, '');
    content = content.replace(/for \(const \[key, component\] of Object\.entries\(ElementPlusIconsVue\)\) \{[\s\S]*?app\.component\(key, component\)[\s\S]*?\}/g, '');
    
    // 添加 uView 导入和注册
    if (!content.includes('uview-ui')) {
      const uviewImport = "import uView from 'uview-ui'\n";
      const uviewUse = "app.use(uView)\n";
      
      // 在 createApp 之后添加 uView 注册
      content = content.replace(/(const app = createApp\(App\))/, `$1\n${uviewUse}`);
      
      // 在文件顶部添加 uView 导入
      const lines = content.split('\n');
      const importIndex = lines.findIndex(line => line.includes('import'));
      if (importIndex !== -1) {
        lines.splice(importIndex, 0, uviewImport);
        content = lines.join('\n');
      }
    }
    
    fs.writeFileSync(mainJsPath, content, 'utf8');
    console.log(`✅ 已更新: ${mainJsPath}`);
    return true;
  } catch (error) {
    console.error(`❌ 更新失败: ${mainJsPath}`, error.message);
    return false;
  }
}

function updatePackageJson(packageJsonPath) {
  try {
    const content = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
    
    // 移除 Element Plus 依赖
    if (content.dependencies) {
      delete content.dependencies['element-plus'];
      delete content.dependencies['@element-plus/icons-vue'];
    }
    
    // 添加 uView 依赖
    if (!content.dependencies['uview-ui']) {
      content.dependencies['uview-ui'] = '^2.0.36';
    }
    
    fs.writeFileSync(packageJsonPath, JSON.stringify(content, null, 2), 'utf8');
    console.log(`✅ 已更新: ${packageJsonPath}`);
    return true;
  } catch (error) {
    console.error(`❌ 更新失败: ${packageJsonPath}`, error.message);
    return false;
  }
}

function main() {
  const args = process.argv.slice(2);
  const targetPath = args[0] || './vue01/src';
  
  if (!fs.existsSync(targetPath)) {
    console.error(`❌ 路径不存在: ${targetPath}`);
    process.exit(1);
  }
  
  console.log('🚀 开始替换 Element Plus 到 uView UI...');
  console.log(`📁 目标路径: ${targetPath}`);
  
  // 处理文件
  const processedCount = processDirectory(targetPath);
  
  // 更新 main.js
  const mainJsPath = path.join(targetPath, '../main.js');
  if (fs.existsSync(mainJsPath)) {
    updateMainJs(mainJsPath);
  }
  
  // 更新 package.json
  const packageJsonPath = path.join(targetPath, '../../package.json');
  if (fs.existsSync(packageJsonPath)) {
    updatePackageJson(packageJsonPath);
  }
  
  console.log(`\n🎉 替换完成！共处理了 ${processedCount} 个文件`);
  console.log('\n📝 后续步骤:');
  console.log('1. 运行 npm install 安装 uView UI');
  console.log('2. 检查并修复可能的语法错误');
  console.log('3. 测试功能完整性');
  console.log('4. 调整样式和布局');
}

if (require.main === module) {
  main();
}

module.exports = {
  replaceComponents,
  processFile,
  processDirectory,
  updateMainJs,
  updatePackageJson
}; 