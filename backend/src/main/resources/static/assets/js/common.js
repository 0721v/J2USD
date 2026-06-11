// 多语言消息
const messages = {
  zh: {
    common: {
      all: '全部',
      success: '成功',
      error: '错误',
      submit: '提交',
      refresh: '刷新'
    },
    nav: {
      home: '首页',
      products: '商品',
      redeem: '查询',
      admin: '管理'
    },
    hero: {
      title: '兰雀Pro',
      subtitle: '安全便捷的礼品卡发卡平台',
      getStarted: '立即开始'
    },
    payment: {
      title: '支持多种支付方式',
      wechat: '微信支付',
      alipay: '支付宝',
      usdt: 'USDT (TRC20)',
      okx: 'OKX（TRC20）',
      okxDesc: '通过 OKX 交易所直接转账 USDT',
      okxAddress: 'OKX 收款地址',
      okxAmount: 'USDT 金额',
      okxStep1: '打开 OKX 交易所 App 或网页版',
      okxStep2: '在资金页面选择提币/转账',
      okxStep3: '输入上方收款地址，转账 {amount} USDT',
      okxStep4: '完成转账后等待系统确认（约 1-3 分钟）',
      scanQR: '请扫描二维码完成支付',
      trc20Address: 'TRC20地址',
      trc20Amount: 'USDT金额',
      copyAddress: '复制地址',
      copyAmount: '复制金额',
      waiting: '等待支付确认...',
      success: '支付成功！',
      expireIn: '订单将在 {minutes} 分钟后过期',
      okxRecommend: '推荐使用欧易交易所',
      okxDesc: '如果您没有 USDT 钱包，建议使用欧易交易所进行转账',
      okxLink: '前往欧易交易所',
      step1: '打开您的 USDT 钱包（TRC20 网络）',
      step2: '转账 {amount} USDT 到上方地址',
      step3: '备注/附言中填写：{orderNo}',
      step4: '完成转账后等待系统确认（约 1-3 分钟）'
    },
    how: {
      title: '如何购买',
      step1: '选择商品',
      step1Desc: '浏览我们的商品列表',
      step2: '完成支付',
      step2Desc: '选择您喜欢的支付方式',
      step3: '获取卡密',
      step3Desc: '兑换码将发送到您的邮箱'
    },
    footer: {
      rights: '© 2024 保留所有权利'
    },
    product: {
      title: '商品列表',
      price: '价格',
      originalPrice: '原价',
      stock: '库存',
      sold: '已售',
      buyNow: '立即购买',
      outOfStock: '暂时缺货'
    },
    order: {
      title: '创建订单',
      product: '商品',
      quantity: '数量',
      total: '总计',
      paymentMethod: '支付方式',
      wechat: '微信支付',
      alipay: '支付宝',
      trc20: 'USDT (TRC20)',
      okx: 'OKX（TRC20）',
      email: '邮箱',
      phone: '手机',
      createOrder: '创建订单',
      orderNo: '订单号',
      status: '状态',
      pending: '待支付',
      paid: '已支付',
      completed: '已完成',
      cancelled: '已取消',
      expired: '已过期',
      payNow: '立即支付',
      copyCode: '复制兑换码',
      redeem: '兑换'
    }
  },
  en: {
    common: {
      all: 'All',
      success: 'Success',
      error: 'Error',
      submit: 'Submit',
      refresh: 'Refresh'
    },
    nav: {
      home: 'Home',
      products: 'Products',
      redeem: 'Query',
      admin: 'Admin'
    },
    hero: {
      title: 'Secure & Convenient Gift Card Platform',
      subtitle: 'Multiple payment methods, instant delivery',
      getStarted: 'Get Started'
    },
    payment: {
      title: 'Multiple Payment Methods Supported',
      wechat: 'WeChat Pay',
      alipay: 'Alipay',
      usdt: 'USDT (TRC20)',
      okx: 'OKX (TRC20)',
      okxDesc: 'Transfer USDT directly via OKX Exchange',
      okxAddress: 'OKX Deposit Address',
      okxAmount: 'USDT Amount',
      okxStep1: 'Open OKX Exchange App or website',
      okxStep2: 'Go to Assets and select Withdraw/Transfer',
      okxStep3: 'Enter the address above and transfer {amount} USDT',
      okxStep4: 'Wait for system confirmation after transfer (about 1-3 minutes)',
      scanQR: 'Please scan the QR code to complete payment',
      trc20Address: 'TRC20 Address',
      trc20Amount: 'USDT Amount',
      copyAddress: 'Copy Address',
      copyAmount: 'Copy Amount',
      waiting: 'Waiting for payment confirmation...',
      success: 'Payment Successful!',
      expireIn: 'Order expires in {minutes} minutes',
      okxRecommend: 'Recommended: OKX Exchange',
      okxDesc: 'If you don\'t have a USDT wallet, we recommend using OKX Exchange for transfer',
      okxLink: 'Go to OKX Exchange',
      step1: 'Open your USDT wallet (TRC20 network)',
      step2: 'Transfer {amount} USDT to the address above',
      step3: 'Fill in the memo/remark: {orderNo}',
      step4: 'Wait for system confirmation after transfer (about 1-3 minutes)'
    },
    how: {
      title: 'How to Buy',
      step1: 'Select Product',
      step1Desc: 'Browse our product catalog',
      step2: 'Make Payment',
      step2Desc: 'Choose your preferred payment method',
      step3: 'Get Card Code',
      step3Desc: 'Redemption code sent to your email'
    },
    footer: {
      rights: '© 2024 All Rights Reserved'
    },
    product: {
      title: 'Products',
      price: 'Price',
      originalPrice: 'Original Price',
      stock: 'Stock',
      sold: 'Sold',
      buyNow: 'Buy Now',
      outOfStock: 'Out of Stock'
    },
    order: {
      title: 'Create Order',
      product: 'Product',
      quantity: 'Quantity',
      total: 'Total',
      paymentMethod: 'Payment Method',
      wechat: 'WeChat Pay',
      alipay: 'Alipay',
      trc20: 'USDT (TRC20)',
      okx: 'OKX Pay',
      email: 'Email',
      phone: 'Phone',
      createOrder: 'Create Order',
      orderNo: 'Order No',
      status: 'Status',
      pending: 'Pending',
      paid: 'Paid',
      completed: 'Completed',
      cancelled: 'Cancelled',
      expired: 'Expired',
      payNow: 'Pay Now',
      copyCode: 'Copy Code',
      redeem: 'Redeem'
    }
  },
  ja: {
    common: {
      all: 'すべて',
      success: '成功',
      error: 'エラー',
      submit: '送信',
      refresh: '更新'
    },
    nav: {
      home: 'ホーム',
      products: '商品',
      redeem: '照会',
      admin: '管理'
    },
    hero: {
      title: '安全で便利なギフトカードプラットフォーム',
      subtitle: '複数の支払い方法、即時配送',
      getStarted: '今すぐ始める'
    },
    payment: {
      title: '複数の支払い方法に対応',
      wechat: 'WeChatペイ',
      alipay: 'Alipay',
      usdt: 'USDT (TRC20)',
      okx: 'OKX（TRC20）',
      okxDesc: 'OKX取引所から直接USDTを送金',
      okxAddress: 'OKX受取アドレス',
      okxAmount: 'USDT金額',
      okxStep1: 'OKX取引所のアプリまたはウェブを開く',
      okxStep2: '資産ページで出金/送金を選択',
      okxStep3: '上記アドレスを入力し、{amount} USDTを送金',
      okxStep4: '送金後、システム確認を待つ（約1-3分）',
      scanQR: 'QRコードをスキャンして支払いを完了してください',
      trc20Address: 'TRC20アドレス',
      trc20Amount: 'USDT金額',
      copyAddress: 'アドレスをコピー',
      copyAmount: '金額をコピー',
      waiting: '支払い確認を待っています...',
      success: '支払いが完了しました！',
      expireIn: '注文は{minutes}分後に期限切れになります',
      okxRecommend: 'おすすめ：OKX取引所',
      okxDesc: 'USDTウォレットをお持ちでない場合は、OKX取引所での送金をおすすめします',
      okxLink: 'OKX取引所へ',
      step1: 'USDTウォレットを開く（TRC20ネットワーク）',
      step2: '{amount} USDTを上記アドレスに送金',
      step3: '備考/メモに記入：{orderNo}',
      step4: '送金後、システム確認を待つ（約1-3分）'
    },
    how: {
      title: '購入方法',
      step1: '商品を選択',
      step1Desc: '商品カタログを閲覧する',
      step2: '支払いを完了',
      step2Desc: 'お好みの支払い方法を選択',
      step3: 'コードを取得',
      step3Desc: ' redemption codeがメールに送信されます'
    },
    footer: {
      rights: '© 2024 All Rights Reserved'
    },
    product: {
      title: '商品一覧',
      price: '価格',
      originalPrice: '通常価格',
      stock: '在庫',
      sold: '販売数',
      buyNow: '今すぐ購入',
      outOfStock: '在庫切れ'
    },
    order: {
      title: '注文作成',
      product: '商品',
      quantity: '数量',
      total: '合計',
      paymentMethod: '支払い方法',
      wechat: 'WeChatペイ',
      alipay: 'Alipay',
      trc20: 'USDT (TRC20)',
      okx: 'OKXペイ',
      email: 'メール',
      phone: '電話',
      createOrder: '注文を作成',
      orderNo: '注文番号',
      status: 'ステータス',
      pending: '支払い待ち',
      paid: '支払い済み',
      completed: '完了',
      cancelled: 'キャンセル',
      expired: '期限切れ',
      payNow: '今すぐ支払う',
      copyCode: 'コードをコピー',
      redeem: '使用する'
    }
  },
  ko: {
    common: {
      all: '전체',
      success: '성공',
      error: '오류',
      submit: '제출',
      refresh: '새로고침'
    },
    nav: {
      home: '홈',
      products: '상품',
      redeem: '조회',
      admin: '관리'
    },
    hero: {
      title: '안전하고 편리한 기프트카드 플랫폼',
      subtitle: '다양한 결제 방법, 즉시 배송',
      getStarted: '시작하기'
    },
    payment: {
      title: '다양한 결제 방법 지원',
      wechat: '위챗 페이',
      alipay: '알리페이',
      usdt: 'USDT (TRC20)',
      okx: 'OKX（TRC20）',
      okxDesc: 'OKX 거래소에서 직접 USDT 송금',
      okxAddress: 'OKX 수신 주소',
      okxAmount: 'USDT 금액',
      okxStep1: 'OKX 거래소 앱 또는 웹사이트 열기',
      okxStep2: '자산 페이지에서 출금/송금 선택',
      okxStep3: '위 주소를 입력하고 {amount} USDT 송금',
      okxStep4: '송금 후 시스템 확인 대기 (약 1-3분)',
      scanQR: 'QR 코드를 스캔하여 결제를 완료하세요',
      trc20Address: 'TRC20 주소',
      trc20Amount: 'USDT 금액',
      copyAddress: '주소 복사',
      copyAmount: '금액 복사',
      waiting: '결제 확인 대기 중...',
      success: '결제가 완료되었습니다!',
      expireIn: '주문은 {minutes}분 후에 만료됩니다',
      okxRecommend: '추천: OKX 거래소',
      okxDesc: 'USDT 지갑이 없으시면 OKX 거래소를 이용한 송금을 추천드립니다',
      okxLink: 'OKX 거래소로 이동',
      step1: 'USDT 지갑 열기 (TRC20 네트워크)',
      step2: '{amount} USDT를 위 주소로 송금',
      step3: '메모/비고란에 입력: {orderNo}',
      step4: '송금 후 시스템 확인 대기 (약 1-3분)'
    },
    how: {
      title: '구매 방법',
      step1: '상품 선택',
      step1Desc: '상품 카탈로그를 둘러보세요',
      step2: '결제 완료',
      step2Desc: '선호하는 결제 방법을 선택하세요',
      step3: '코드 받기',
      step3Desc: 'redeem code가 이메일로 전송됩니다'
    },
    footer: {
      rights: '© 2024 All Rights Reserved'
    },
    product: {
      title: '상품 목록',
      price: '가격',
      originalPrice: '정가',
      stock: '재고',
      sold: '판매량',
      buyNow: '바로 구매',
      outOfStock: '품절'
    },
    order: {
      title: '주문 생성',
      product: '상품',
      quantity: '수량',
      total: '총액',
      paymentMethod: '결제 방법',
      wechat: '위챗 페이',
      alipay: '알리페이',
      trc20: 'USDT (TRC20)',
      okx: 'OKX 페이',
      email: '이메일',
      phone: '전화',
      createOrder: '주문 생성',
      orderNo: '주문번호',
      status: '상태',
      pending: '결제 대기',
      paid: '결제 완료',
      completed: '완료',
      cancelled: '취소',
      expired: '만료',
      payNow: '지금 결제',
      copyCode: '코드 복사',
      redeem: '사용하기'
    }
  }
};

// 获取当前语言
function getCurrentLang() {
  const urlParams = new URLSearchParams(window.location.search);
  let lang = urlParams.get('lang');
  
  if (!lang) {
    lang = localStorage.getItem('giftcard_lang') || 'zh';
  }
  
  // 确保语言有效
  if (!['zh', 'en', 'ja', 'ko'].includes(lang)) {
    lang = 'zh';
  }
  
  return lang;
}

// 设置语言
function setLang(lang) {
  if (['zh', 'en', 'ja', 'ko'].includes(lang)) {
    localStorage.setItem('giftcard_lang', lang);
    // 刷新页面以应用新语言
    window.location.reload();
  }
}

// 获取消息
function getMessages() {
  const lang = getCurrentLang();
  return messages[lang] || messages.zh;
}

// 导航到指定页面
function navigateTo(url) {
  // 创建或获取过渡遮罩层
  let overlay = document.querySelector('.page-transition-overlay');
  if (!overlay) {
    overlay = document.createElement('div');
    overlay.className = 'page-transition-overlay';
    document.body.appendChild(overlay);
  }
  
  // 显示遮罩层（淡出效果）
  overlay.classList.add('active');
  
  // 延迟跳转，让动画有时间执行
  setTimeout(() => {
    // 获取当前页面路径
    const currentPath = window.location.pathname;
    
    // 如果当前在 pages/ 目录下
    if (currentPath.includes('/pages/')) {
      // 从当前路径中提取根目录
      const rootPath = currentPath.substring(0, currentPath.indexOf('/pages/'));
      
      // 目标路径以 pages/ 开头，或者是 admin/ 开头，都需要加上根路径
      if (url.startsWith('pages/') || url.startsWith('admin/')) {
        window.location.href = rootPath + '/' + url;
        return;
      }
    }
    
    window.location.href = url;
  }, 200); // 200ms 的过渡时间
}

// 切换移动端菜单
function toggleMobileMenu() {
  const menu = document.getElementById('mobileMenu');
  const icon = document.getElementById('mobileMenuIcon');
  
  if (menu.classList.contains('hidden')) {
    menu.classList.remove('hidden');
    icon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>';
  } else {
    menu.classList.add('hidden');
    icon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>';
  }
}

// 显示Toast消息
function showToast(message, type = 'info') {
  const colors = {
    success: 'bg-green-500',
    error: 'bg-red-500',
    info: 'bg-blue-500',
    warning: 'bg-yellow-500'
  };
  
  const toast = document.createElement('div');
  toast.className = `fixed top-20 left-1/2 transform -translate-x-1/2 px-6 py-3 rounded-lg text-white ${colors[type]} z-99999 shadow-lg`;
  toast.textContent = message;
  toast.style.zIndex = '99999';
  
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// 格式化时间
function formatTime(timestamp) {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

// ========== 网站设置 ==========

// 全局网站设置缓存
let _siteSettings = null;

// 从API加载网站设置
async function loadSiteSettings() {
  try {
    const response = await fetch('/api/site-settings');
    const data = await response.json();
    if (data.code === 200) {
      _siteSettings = data.data;
      // 缓存到 localStorage，避免每次刷新都请求
      localStorage.setItem('site_settings', JSON.stringify(_siteSettings));
      return _siteSettings;
    }
  } catch (e) {
    console.warn('加载网站设置失败，使用缓存:', e);
  }
  // 降级：从缓存读取
  const cached = localStorage.getItem('site_settings');
  if (cached) {
    _siteSettings = JSON.parse(cached);
  }
  return _siteSettings || { site_name: '兰雀Pro', site_logo: '' };
}

// 获取网站设置（同步，优先用缓存）
function getSiteSettings() {
  if (_siteSettings) return _siteSettings;
  const cached = localStorage.getItem('site_settings');
  if (cached) {
    _siteSettings = JSON.parse(cached);
    return _siteSettings;
  }
  return { site_name: '兰雀Pro', site_logo: '' };
}

// 获取当前语言的网站名称
function getSiteName(lang) {
  const settings = getSiteSettings();
  if (lang && settings['site_name_' + lang]) {
    return settings['site_name_' + lang];
  }
  return settings.site_name || '兰雀Pro';
}

// 获取网站 Logo URL
function getSiteLogo() {
  const settings = getSiteSettings();
  return settings.site_logo || '';
}

// 更新页面中所有网站名称和 Logo
function applySiteSettings(lang) {
  lang = lang || getCurrentLang();
  const name = getSiteName(lang);
  const logo = getSiteLogo();

  // 更新所有 .site-name 元素
  document.querySelectorAll('.site-name').forEach(el => {
    el.textContent = name;
  });

  // 更新页面标题
  if (name) {
    document.title = name;
  }

  // 更新所有 .site-logo 元素
  document.querySelectorAll('.site-logo').forEach(el => {
    if (logo) {
      el.src = logo;
      el.classList.remove('hidden');
    } else {
      el.classList.add('hidden');
    }
  });

  // 更新所有 .site-logo-container 元素
  document.querySelectorAll('.site-logo-container').forEach(el => {
    if (logo) {
      el.innerHTML = '<img src="' + logo + '" alt="Logo" class="h-8 w-auto object-contain">';
      el.classList.remove('hidden');
    } else {
      el.classList.add('hidden');
    }
  });

  // 更新所有 .default-logo 元素（默认的 SVG 图标）
  document.querySelectorAll('.default-logo').forEach(el => {
    if (logo) {
      el.classList.add('hidden');
    } else {
      el.classList.remove('hidden');
    }
  });
}

// ========== Admin 登录检查 ==========

/**
 * 获取 admin token
 */
function getAdminToken() {
  return localStorage.getItem('adminToken');
}

/**
 * 检查 admin 是否已登录，未登录则跳转到登录页
 * 在每个 admin 页面的 DOMContentLoaded 中调用
 */
async function checkAdminAuth() {
  const token = getAdminToken();
  if (!token) {
    window.location.href = '/admin/login.html';
    return false;
  }

  try {
    const response = await fetch('/api/admin/check-auth', {
      headers: {
        'X-Admin-Token': token
      }
    });
    const data = await response.json();

    if (data.code === 200 && data.data && data.data.authenticated) {
      return true;
    } else {
      // token 无效，清除并跳转
      localStorage.removeItem('adminToken');
      localStorage.removeItem('adminUser');
      window.location.href = '/admin/login.html';
      return false;
    }
  } catch (error) {
    console.error('检查登录状态失败:', error);
    // 网络错误时不跳转，允许页面正常加载
    return true;
  }
}

/**
 * admin API 请求封装（自动带上 token）
 */
function adminFetch(url, options = {}) {
  const token = getAdminToken();
  if (!options.headers) options.headers = {};
  if (token) {
    options.headers['X-Admin-Token'] = token;
  }
  // 如果 body 是 FormData，不设置 Content-Type，让浏览器自动处理
  if (!options.headers['Content-Type'] && !(options.body instanceof FormData)) {
    options.headers['Content-Type'] = 'application/json';
  }
  return fetch(url, options).then(response => {
    if (response.status === 401) {
      localStorage.removeItem('adminToken');
      localStorage.removeItem('adminUser');
      window.location.href = '/admin/login.html';
      return Promise.reject(new Error('未登录'));
    }
    return response;
  });
}

/**
 * admin 退出登录
 */
function adminLogout() {
  localStorage.removeItem('adminToken');
  localStorage.removeItem('adminUser');
  window.location.href = '/admin/login.html';
}
