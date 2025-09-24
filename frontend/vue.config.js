const path = require('path');
const CompressionPlugin = require('compression-webpack-plugin');

function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = {
  productionSourceMap: false,

  configureWebpack: (config) => {
    if (process.env.NODE_ENV === 'production') {
      // 去掉 console
      if (config.optimization?.minimizer?.[0]?.options?.terserOptions?.compress) {
        config.optimization.minimizer[0].options.terserOptions.compress.drop_console = true;
      }

      // 只保留 vendors 分组，其余不拆
      config.optimization = {
        ...config.optimization,
        runtimeChunk: false,
        splitChunks: {
          chunks: 'all',
          // 只定义一个 vendors 分组，合并所有 node_modules
          cacheGroups: {
            vendors: {
              test: /[\\/]node_modules[\\/]/,
              name: 'chunk-vendors',
              chunks: 'all',
              priority: 10,
              enforce: true,
              minSize: 0,
              minChunks: 1,
              reuseExistingChunk: true,
            },
          },
        },
      };
    }
  },

  chainWebpack: (config) => {
    config.resolve.alias
      .set('@', resolve('src'))
      .set('@api', resolve('src/api'))
      .set('@assets', resolve('src/assets'))
      .set('@comp', resolve('src/components'))
      .set('@views', resolve('src/views'));

    // 🔑 禁用预加载/预取（不是必须，但可避免多余 link 提示）
    config.plugins.delete('prefetch');
    config.plugins.delete('preload');

    // 🔑 生产环境把所有动态 import 转为 require（不再生成异步 chunk）
    if (process.env.NODE_ENV === 'production') {
      config.module
        .rule('js')
        .use('babel-loader')
        .tap((opts = {}) => {
          opts.plugins = opts.plugins || [];
          if (!opts.plugins.includes('dynamic-import-node')) {
            opts.plugins.push('dynamic-import-node');
          }
          return opts;
        });

      // 仍保留 gzip
      config.plugin('compressionPlugin').use(
        new CompressionPlugin({
          test: /\.(js|css|less)$/,
          threshold: 10240,
          deleteOriginalAssets: false,
        }),
      );
    }

    // 你的其他 loader 规则
    config.module
      .rule('markdown')
      .test(/\.md$/)
      .use('file-loader')
      .loader('file-loader')
      .end();

    config.module
      .rule('vxe')
      .test(/\.js$/)
      .include.add(resolve('node_modules/vxe-table'))
      .add(resolve('node_modules/vxe-table-plugin-antd'))
      .end()
      .use('babel-loader')
      .loader('babel-loader')
      .end();
  },

  css: {
    loaderOptions: {
      less: {
        modifyVars: {
          'primary-color': '#1890FF',
          'link-color': '#1890FF',
          'border-radius-base': '4px',
        },
        javascriptEnabled: true,
      },
    },
  },

  devServer: {
    port: 3000,
    allowedHosts: ['gdhfi.net.cn', 'gdhfi.org.cn', '43.134.112.36'],
  },
};
