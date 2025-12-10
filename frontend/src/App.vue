<script setup>
import { useRoute } from "vue-router"; // 이게 꼭 있어야 합니다!
import Header from "@/components/common/Header.vue";

const route = useRoute();
</script>

<template>
  <div class="app-container">
    <Header v-if="!route?.meta?.hideHeader" />

    <router-view />
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
  background-color: #050510;
}
</style>
