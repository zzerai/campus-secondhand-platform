<template>
  <div :class="[classObj, { 'is-resizing': resizing }]" class="app-wrapper" :style="{ '--current-color': theme, '--current-color-light': theme + '1a', '--current-color-dark-bg': theme + '33', '--app-sidebar-width': sidebarWidth + 'px' }">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>
    <sidebar v-if="!sidebar.hide" class="sidebar-container" />
    <div
      v-if="device !== 'mobile' && sidebar.opened && !sidebar.hide"
      class="sidebar-resizer"
      :class="{ 'is-resizing': resizing }"
      title="拖动调整侧边栏宽度"
      @mousedown.prevent="startResize"
    ></div>
    <div :class="{ hasTagsView: needTagsView, sidebarHide: sidebar.hide }" class="main-container">
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar @setLayout="setLayout" />
        <tags-view v-if="needTagsView" />
      </div>
      <app-main />
      <settings ref="settingRef" />
    </div>
  </div>
</template>

<script setup>
import { useWindowSize } from '@vueuse/core'
import Sidebar from './components/Sidebar/index.vue'
import { AppMain, Navbar, Settings, TagsView } from './components'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'

const settingsStore = useSettingsStore()
const theme = computed(() => settingsStore.theme)
const sidebar = computed(() => useAppStore().sidebar)
const device = computed(() => useAppStore().device)
const needTagsView = computed(() => settingsStore.tagsView)
const fixedHeader = computed(() => settingsStore.fixedHeader)

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

const { width, height } = useWindowSize()
const WIDTH = 992 // refer to Bootstrap's responsive design

watch(() => device.value, () => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    useAppStore().closeSideBar({ withoutAnimation: false })
  }
})

watchEffect(() => {
  if (width.value - 1 < WIDTH) {
    useAppStore().toggleDevice('mobile')
    useAppStore().closeSideBar({ withoutAnimation: true })
  } else {
    useAppStore().toggleDevice('desktop')
  }
})

function handleClickOutside() {
  useAppStore().closeSideBar({ withoutAnimation: false })
}

const settingRef = ref(null)
function setLayout() {
  settingRef.value.openSetting()
}

// —— 侧边栏宽度拖拽调整 ——
const SIDEBAR_MIN = 180
const SIDEBAR_MAX = 360
function clampWidth(w) {
  return Math.min(SIDEBAR_MAX, Math.max(SIDEBAR_MIN, w))
}
const storedWidth = Number(localStorage.getItem('sidebarWidth'))
const sidebarWidth = ref(storedWidth ? clampWidth(storedWidth) : 200)
const resizing = ref(false)

function onResize(e) {
  // 侧栏固定在最左侧，光标 X 即为期望宽度
  sidebarWidth.value = clampWidth(e.clientX)
}
function stopResize() {
  resizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
  localStorage.setItem('sidebarWidth', String(sidebarWidth.value))
}
function startResize() {
  resizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}
onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style lang="scss" scoped>
@use "@/assets/styles/mixin.scss" as mix;
@use "@/assets/styles/variables.module.scss" as vars;

.app-wrapper {
  @include mix.clearfix;
  position: relative;
  height: 100%;
  width: 100%;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.main-container:has(.fixed-header) {
  height: 100vh;
  overflow: hidden;
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - var(--app-sidebar-width, #{vars.$base-sidebar-width}));
  transition: width 0.28s;
}

/* 侧边栏宽度拖拽手柄 */
.sidebar-resizer {
  position: fixed;
  top: 0;
  bottom: 0;
  left: var(--app-sidebar-width, #{vars.$base-sidebar-width});
  width: 8px;
  margin-left: -4px;
  z-index: 1002;
  cursor: col-resize;

  &::after {
    content: "";
    position: absolute;
    top: 0;
    bottom: 0;
    left: 50%;
    width: 2px;
    transform: translateX(-50%);
    background: transparent;
    transition: background .2s ease;
  }
  &:hover::after,
  &.is-resizing::after {
    background: var(--ct-primary, #127c68);
  }
}

/* 拖拽过程中关闭过渡动画并禁用选中，让侧栏实时跟随光标 */
.app-wrapper.is-resizing {
  user-select: none;
  cursor: col-resize;

  .sidebar-container,
  .main-container,
  .fixed-header {
    transition: none !important;
  }
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px);
}

.sidebarHide .fixed-header {
  width: 100%;
}

.mobile .fixed-header {
  width: 100%;
}
</style>