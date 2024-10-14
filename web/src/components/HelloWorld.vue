<template>
  <div class="container">

    <div class="hello div2 time-display" >
       {{ currentTime }}
    </div>
    <div class="hello div1" >
      <h1>{{ msg }}</h1>
      <p id="typewriter"></p>
   
    </div>


  </div>

</template>


<script>
import TypeWriter from "@/utils/TypeWriter";
import DateUtils from "@/utils/DateUtils";
export default {
  name: 'HelloWorld',
  data() {
    return {
      currentTime: DateUtils.getCurrentTime()
    }
  },
  props: {
    msg: String
  },
  methods: {
    startTypeWriter() {
      const element = this.$el.querySelector('#typewriter');
      // console.info(element)
      const typeWriter = new TypeWriter(element, "人生苦短，先用python，再用java", 80);
      typeWriter.start(() => {
        setTimeout(() => {
          this.startTypeWriter();
        }, 2000) // 2秒后重新开始
      });
    },
    updateTime() {
      this.currentTime = DateUtils.getCurrentTime()
    }
  },
  mounted() {
    this.startTypeWriter();
    setInterval(this.updateTime, 1000); // 每秒更新一次时间
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
#typewriter {
  font-family: 'Courier New', Courier, monospace;
  font-size: 24px;
  white-space: pre-wrap; /* 保证超出宽度时换行 */
  word-wrap: break-word; /* 单词过长时进行断行 */
}

.hello {
 
  width: auto; /* 使用视口宽度的80% */
  height: auto; /* 高度自适应内容 */
  max-width: 600px; /* 最大宽度为300px */
  padding: 0px 10px;
  box-sizing: border-box; /* 包括内边距在内的宽度计算 */
  // border: 1px solid #000;
  border-radius: 10%;
  // margin: 10px; /* 添加间距 */

}

.div1 {
  width: 500px;
  top: 35%;
  // left: 10%;
  text-align: center;
}

.div2 {
  width: 300px;
  // height: 100px;
  // top: 45%;
  // left: 50%;
  text-align: center;
  
}

// 在自适应
@media (max-width: 600px) {
  .div1 {
    width: auto;
    left: auto;
    height: 200px;
  }
  .container {
    display: flex;
    flex-direction: column; /* 在小屏幕上垂直排列 */
    align-items: center; /* 垂直居中对齐 */
  }
  .hello {
    position: relative;
    width: 100%; /* 在小屏幕上占满宽度 */
    max-width: 300px; /* 最大宽度300px */
  }
}
.container {
  display: flex; /* 使用 Flexbox 布局 */
  justify-content: center; /* 中心对齐 */
  align-items: center; /* 顶部对齐 */
  flex-wrap: wrap; /* 自动换行以适应小屏幕 */
  flex-direction: column; 
  // gap: 20px; /* 设置两个div之间的间距 */
}


.time-display {
  margin-top: 20px; /* 添加顶部间距 */
  font-size: 20px; /* 设置字体大小 */
  
}

</style>
