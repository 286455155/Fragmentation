package me.yokeyword.fragmentation;

import android.view.MotionEvent;

import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * Created by YoKey on 17/6/13.
 */

public interface ISupportActivity {
    SupportActivityDelegate getSupportDelegate();

    SupportTransaction supportTransaction();

    FragmentAnimator getFragmentAnimator();

    void setFragmentAnimator(FragmentAnimator fragmentAnimator);

    FragmentAnimator onCreateFragmentAnimator();

    void onBackPressed();

    void onBackPressedSupport();

    boolean dispatchTouchEvent(MotionEvent ev);

    /**
     * Load root fragment, and push to back stack.
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    void loadRootFragment(int containerId, SupportFragment toFragment);

    /**
     * Load root fragment.
     * @param addToBackStack push to back stack
     * @param allowAnimation allow show animation
     */
    void loadRootFragment(int containerId, SupportFragment toFragment, boolean addToBackStack, boolean allowAnimation);

    /**
     * 加载多个根Fragment
     *
     * @param containerId 容器id
     * @param toFragments 目标Fragments
     */
    void loadMultipleRootFragment(int containerId, int showPosition, SupportFragment... toFragments);

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     * <p>
     * 建议使用更明确的{@link #showHideFragment(SupportFragment, SupportFragment)}
     *
     * @param showFragment 需要show的Fragment
     */
    @Deprecated
    void showHideFragment(SupportFragment showFragment);

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    void showHideFragment(SupportFragment showFragment, SupportFragment hideFragment);

    /**
     * 启动目标Fragment
     *
     * @param toFragment 目标Fragment
     */
    void start(SupportFragment toFragment);

    void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode);

    void startForResult(SupportFragment toFragment, int requestCode);

    void startWithPop(SupportFragment toFragment);

    void pop();

    /**
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment);

    void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable);

    void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim);
}
