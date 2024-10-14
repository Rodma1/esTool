<template>
  
  
  

  <div class="container">

      <div  class="home" style="margin-top: 15%;">
      <img  alt="Vue logo"  src="../assets/logo.png">

    </div>

    <HelloWorld msg="神的孩子都在歌唱个人主页"/>
    </div>



</template>

<script>
// @ is an alias to /src
import HelloWorld from '@/components/HelloWorld.vue'

export default {
  name: 'HomeView',
  components: {
    HelloWorld
  }
}
</script>
<style lang="scss">
// .div1 {
//   display: flex;
//   align-items: center; /* 垂直居中对齐 */
//   flex-direction: column; /* 在小屏幕上垂直排列 */
//   -webkit-align-self: center;
//   align-self: center;
// }

.container {
  display: flex;
  // width: 100%;
  // height: 100%;
  align-items: center; /* 垂直居中对齐 */
  flex-direction: column; /* 在小屏幕上垂直排列 */

}

.home {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  
}



</style>