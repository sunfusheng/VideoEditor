package com.sunfusheng.mvvm.ktx

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
fun <T : ViewModel> FragmentActivity.getViewModel(modelClass: Class<T>): T {
  return ViewModelProvider(this)[modelClass]
}