<template>
  <div class="footer">
    <div class="copyright">
      &copy; 2024 HFI 图灵社 | HFI Coding Club.
      <a href="https://github.com/Amazingkenneth/gfc" target="_blank">Source code</a> is open source under the
      <a href="https://opensource.org/licenses/Apache-2.0" target="_blank">Apache 2.0 License</a>.
    </div>
  </div>
</template>

<script>
export default {
  name: "LayoutFooter"
}
</script>

<style lang="less" scoped>
.footer {
  padding: 0 16px;
  margin: 48px 0 24px;
  text-align: center;

  a {
    color: rgba(0, 0, 0, .45);

    &:hover {
      color: rgba(0, 0, 0, .80);
    }
  }

  .copyright {
    color: rgba(0, 0, 0, .45);
    font-size: 14px;
  }
}
</style>
