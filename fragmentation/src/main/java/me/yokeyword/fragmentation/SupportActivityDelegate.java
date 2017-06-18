package me.yokeyword.fragmentation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;

import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.debug.DebugStackDelegate;

public class SupportActivityDelegate {
    private ISupportActivity mSupport;
    private FragmentActivity mActivity;

    boolean mPopMultipleNoAnim = false;
    boolean mFragmentClickable = true;

    private TransactionDelegate mTransactionDelegate;
    private FragmentAnimator mFragmentAnimator;
    private int mDefaultFragmentBackground = 0;
    private DebugStackDelegate mDebugStackDelegate;
    private SupportHelper mSupportHelper = SupportHelper.getInstance();

    public SupportActivityDelegate(ISupportActivity support) {
        if (!(support instanceof Activity))
            throw new RuntimeException("Must extends FragmentActivity/AppCompatActivity");
        this.mSupport = support;
        this.mActivity = (FragmentActivity) support;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        mTransactionDelegate = new TransactionDelegate(mSupport);
        mDebugStackDelegate = new DebugStackDelegate(mActivity);

        mFragmentAnimator = mSupport.onCreateFragmentAnimator();
        mDebugStackDelegate.onCreate(Fragmentation.getDefault().getMode());
    }

    public TransactionDelegate getTransactionDelegate() {
        if (mTransactionDelegate == null) {
            mTransactionDelegate = new TransactionDelegate(null);
        }
        return mTransactionDelegate;
    }

    public SupportTransaction supportTransaction() {
        return new SupportTransaction.SupportTransactionImpl<>(SupportHelper.getInstance().getTopFragment(mActivity.getSupportFragmentManager()), getTransactionDelegate(), true);
    }

    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        mDebugStackDelegate.onPostCreate(Fragmentation.getDefault().getMode());
    }

    /**
     * 获取设置的全局动画 copy
     *
     * @return FragmentAnimator
     */
    public FragmentAnimator getFragmentAnimator() {
        return new FragmentAnimator(
                mFragmentAnimator.getEnter(), mFragmentAnimator.getExit(),
                mFragmentAnimator.getPopEnter(), mFragmentAnimator.getPopExit()
        );
    }

    /**
     * 设置Fragment内的全局动画
     */
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        this.mFragmentAnimator = fragmentAnimator;
    }

    /**
     * 构建Fragment转场动画
     * <p/>
     * 如果是在Activity内实现,则构建的是Activity内所有Fragment的转场动画,
     * 如果是在Fragment内实现,则构建的是该Fragment的转场动画,此时优先级 > Activity的onCreateFragmentAnimator()
     *
     * @return FragmentAnimator对象
     */
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultVerticalAnimator();
    }

    /**
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变Fragment背景。
     */
    public void setDefaultFragmentBackground(@DrawableRes int backgroundRes) {
        mDefaultFragmentBackground = backgroundRes;
    }

    public int getDefaultFragmentBackground() {
        return mDefaultFragmentBackground;
    }

    /**
     * 显示栈视图dialog,调试时使用
     */
    public void showFragmentStackHierarchyView() {
        mDebugStackDelegate.showFragmentStackHierarchyView();
    }

    /**
     * 显示栈视图日志,调试时使用
     */
    public void logFragmentStackHierarchy(String TAG) {
        mDebugStackDelegate.logFragmentRecords(TAG);
    }

    /**
     * 不建议复写该方法,请使用 {@link #onBackPressedSupport} 代替
     */
    public void onBackPressed() {
        if (!mFragmentClickable) {
            mFragmentClickable = true;
        }

        // 获取activeFragment:即从栈顶开始 状态为show的那个Fragment
        SupportFragment activeFragment = mSupportHelper.getActiveFragment(mActivity.getSupportFragmentManager());
        if (mTransactionDelegate.dispatchBackPressedEvent(activeFragment)) return;

        onBackPressedSupport();
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    public void onBackPressedSupport() {
        if (mActivity.getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            ActivityCompat.finishAfterTransition(mActivity);
        }
    }

    public void onDestroy() {
        mDebugStackDelegate.onDestroy();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 防抖动(防止点击速度过快)
        return !mFragmentClickable;
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    public void loadRootFragment(int containerId, SupportFragment toFragment) {
        loadRootFragment(containerId, toFragment, true, false);
    }

    public void loadRootFragment(int containerId, SupportFragment toFragment, boolean addToBackStack, boolean allowAnimation) {
        mTransactionDelegate.loadRootTransaction(mActivity.getSupportFragmentManager(), containerId, toFragment, addToBackStack, allowAnimation);
    }

    /**
     * 加载多个根Fragment
     *
     * @param containerId 容器id
     * @param toFragments 目标Fragments
     */
    public void loadMultipleRootFragment(int containerId, int showPosition, SupportFragment... toFragments) {
        mTransactionDelegate.loadMultipleRootTransaction(mActivity.getSupportFragmentManager(), containerId, showPosition, toFragments);
    }

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     * <p>
     * 建议使用更明确的{@link #showHideFragment(SupportFragment, SupportFragment)}
     *
     * @param showFragment 需要show的Fragment
     */
    public void showHideFragment(SupportFragment showFragment) {
        showHideFragment(showFragment, null);
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    public void showHideFragment(SupportFragment showFragment, SupportFragment hideFragment) {
        mTransactionDelegate.showHideFragment(mActivity.getSupportFragmentManager(), showFragment, hideFragment);
    }

    /**
     * 启动目标Fragment
     *
     * @param toFragment 目标Fragment
     */
    public void start(SupportFragment toFragment) {
        start(toFragment, SupportFragment.STANDARD);
    }

    public void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode) {
        mTransactionDelegate.dispatchStartTransaction(mActivity.getSupportFragmentManager(), mSupportHelper.getTopFragment(mActivity.getSupportFragmentManager()), toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD);
    }

    public void startForResult(SupportFragment toFragment, int requestCode) {
        mTransactionDelegate.dispatchStartTransaction(mActivity.getSupportFragmentManager(), mSupportHelper.getTopFragment(mActivity.getSupportFragmentManager()), toFragment, requestCode, SupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT);
    }

    public void startWithPop(SupportFragment toFragment) {
        mTransactionDelegate.dispatchStartTransaction(mActivity.getSupportFragmentManager(), mSupportHelper.getTopFragment(mActivity.getSupportFragmentManager()), toFragment, 0, SupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITH_POP);
    }

    /**
     * 出栈
     */
    public void pop() {
        mTransactionDelegate.back(mActivity.getSupportFragmentManager());
    }

    /**
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
        popTo(targetFragmentClass, includeTargetFragment, null);
    }

    /**
     * 用于出栈后,立刻进行FragmentTransaction操作
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable) {
        popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, 0);
    }

    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
        mTransactionDelegate.popTo(targetFragmentClass.getName(), includeTargetFragment, afterPopTransactionRunnable, mActivity.getSupportFragmentManager(), popAnim);
    }
}