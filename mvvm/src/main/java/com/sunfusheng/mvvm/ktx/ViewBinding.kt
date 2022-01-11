package com.sunfusheng.mvvm.ktx

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author sunfusheng
 * @since  2021/12/31
 */

inline fun <reified VB : ViewBinding> FragmentActivity.viewBinding() = lazy {
  inflateBinding<VB>(layoutInflater).apply { setContentView(root) }
}

inline fun <reified VB : ViewBinding> Dialog.viewBinding() = lazy {
  inflateBinding<VB>(layoutInflater).apply { setContentView(root) }
}

inline fun <reified VB : ViewBinding> ViewGroup.viewBinding(
  attachToParent: Boolean = false
) = lazy {
  inflateBinding<VB>(
    LayoutInflater.from(context),
    if (attachToParent) this else null,
    attachToParent
  )
}

inline fun <reified VB : ViewBinding> ViewGroup.viewBinding() =
  inflateBinding<VB>(LayoutInflater.from(context), this, true)

inline fun <reified VB : ViewBinding> inflateBinding(
  layoutInflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean
) = VB::class.java.getMethod(
  "inflate",
  LayoutInflater::class.java,
  ViewGroup::class.java,
  Boolean::class.java
)
  .invoke(null, layoutInflater, parent, attachToParent) as VB

inline fun <reified VB : ViewBinding> inflateBinding(layoutInflater: LayoutInflater) =
  VB::class.java.getMethod("inflate", LayoutInflater::class.java).invoke(null, layoutInflater) as VB

inline fun <reified VB : ViewBinding> Fragment.viewBinding() =
  FragmentBindingDelegate(VB::class.java)

class FragmentBindingDelegate<VB : ViewBinding>(
  private val clazz: Class<VB>
) : ReadOnlyProperty<Fragment, VB> {
  private var binding: VB? = null

  @Suppress("UNCHECKED_CAST")
  override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
    if (binding == null) {
      binding = clazz.getMethod("bind", View::class.java).invoke(null, thisRef.requireView()) as VB
      thisRef.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
          super.onDestroy(owner)
          binding = null
        }
      })
    }
    return binding!!
  }
}