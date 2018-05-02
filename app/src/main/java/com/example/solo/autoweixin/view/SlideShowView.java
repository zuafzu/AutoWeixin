package com.example.solo.autoweixin.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.solo.autoweixin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * ViewPager实现的轮播图广告自定义视图，如京东首页的广告轮播图效果； 既支持自动轮播页面也支持手势滑动切换页面
 */

public class SlideShowView extends FrameLayout {

    // 图片类型，0本地文件件或者网络图片，1resId资源文件
    private int type = 0;
    // 自动轮播的时间间隔
    private int TIME_INTERVAL = 5;
    // 自动轮播启用开关
    private boolean isAutoPlay = true;
    // 是否可以循环
    private boolean isWhile = true;

    // 自定义轮播图的资源
    private String[] imageUrls;
    // 放轮播图片的ImageView 的list
    private List<ImageView> imageViewsList;
    // 放圆点的View的list
    private List<View> dotViewsList;

    private ViewPager viewPager;
    // 当前轮播页
    private int currentItem = 0;
    // 定时任务
    private ScheduledExecutorService scheduledExecutorService;

    private OnMyPageChangeListener listener;

    private Context context;

    public static interface OnSlideShowViewItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnSlideShowViewItemClickListener mOnItemClickListener = null;

    /**
     * 轮播图的点击事件
     *
     * @param listener
     */
    public void setOnItemClickListener(OnSlideShowViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    // Handler
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentItem);
        }

    };

    int width = 0;

    public SlideShowView(Context context) {
        this(context, null);
    }

    public SlideShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideShowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initData();
        if (isAutoPlay) {
            startPlay();
        }
        // 获取屏幕宽度

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;// 屏幕的宽dp
    }

    /**
     * 开始轮播图切换
     */
    private void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), TIME_INTERVAL, TIME_INTERVAL,
                TimeUnit.SECONDS);
    }

    /**
     * 停止轮播图切换
     */
    private void stopPlay() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        isAutoPlay = autoPlay;
        if (isAutoPlay) {
            startPlay();
        } else {
            stopPlay();
        }
    }

    /**
     * 初始化图片之前设置
     */
    public void setWhile(boolean aWhile) {
        isWhile = aWhile;
    }

    public void setListener(OnMyPageChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 初始化相关Data
     */
    private void initData() {
        imageViewsList = new ArrayList<ImageView>();
        dotViewsList = new ArrayList<View>();
    }

    /**
     * 设置自动轮播的时间间隔
     */
    public void setTimeInterval(int timeInterval) {
        stopPlay();
        TIME_INTERVAL = timeInterval;
        startPlay();
    }

    /**
     * 初始化Views等UI
     *
     * @param context    上下文对象
     * @param scale      屏幕宽控件高比例
     * @param mImageUrls 图片List
     */
    public void initUI(Context context, float scale, List<Integer> mImageUrls) {
        int[] imgs = new int[mImageUrls.size()];
        for (int i = 0; i < mImageUrls.size(); i++) {
            imgs[i] = mImageUrls.get(i);
        }
        initUI(context, scale, imgs);
    }

    /**
     * 初始化Views等UI
     *
     * @param context    上下文对象
     * @param scale      屏幕宽控件高比例
     * @param mImageUrls 图片数组
     */
    public void initUI(Context context, float scale, int[] mImageUrls) {
        type = 1;
        String[] mmimageUrls = new String[mImageUrls.length];
        for (int i = 0; i < mImageUrls.length; i++) {
            mmimageUrls[i] = mImageUrls[i] + "";
        }
        initUI(context, scale, mmimageUrls);
    }

    /**
     * 初始化Views等UI
     *
     * @param context    上下文对象
     * @param scale      屏幕宽控件高比例
     * @param mImageUrls 图片数组
     */
    public void initUI(Context context, float scale, String[] mImageUrls) {
        imageViewsList.clear();
        imageUrls = mImageUrls;
        if (imageUrls == null || imageUrls.length == 0) {
            return;
        }
        LinearLayout dotLayout = new LinearLayout(context);
        dotLayout.setPadding(16, 16, 16, 16);
        dotLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        dotLayout.removeAllViews();
        // 热点个数与图片特殊相等
        if (imageUrls.length > 1 && isWhile) {
            ImageView view = new ImageView(context);
            view.setTag(R.string.app_name, imageUrls[imageUrls.length - 1]);
            view.setScaleType(ScaleType.FIT_XY);
            imageViewsList.add(view);
        }
        for (int i = 0; i < imageUrls.length; i++) {
            final int a = i;
            ImageView view = new ImageView(context);
            view.setTag(R.string.app_name, imageUrls[i]);
            view.setScaleType(ScaleType.FIT_XY);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, a);
                    }
                }
            });
            imageViewsList.add(view);
            ImageView dotView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = 4;
            params.rightMargin = 4;
            dotLayout.addView(dotView, params);
            dotViewsList.add(dotView);
        }
        if (imageUrls.length > 1 && isWhile) {
            ImageView view = new ImageView(context);
            view.setTag(R.string.app_name, imageUrls[0]);
            view.setScaleType(ScaleType.FIT_XY);
            imageViewsList.add(view);
        }
        viewPager = new ViewPager(context);
        viewPager.setFocusable(true);
        viewPager.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        if (scale > 0) {
            viewPager.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (width / scale)));
        }
        viewPager.setAdapter(new MyPagerAdapter());
        if (imageUrls.length > 1 && isWhile) {
            currentItem = 1;
            viewPager.setCurrentItem(0, false);
            viewPager.setCurrentItem(1, false);
        } else {
            viewPager.setCurrentItem(0, false);
        }
        viewPager.addOnPageChangeListener(new MyPageChangeListener());
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        relativeLayout.addView(viewPager);
        dotLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        relativeLayout.addView(dotLayout);
        this.addView(relativeLayout);
        for (int i = 0; i < dotViewsList.size(); i++) {
            int mPos = 0;
            if (isWhile) {
                mPos = 0;
                if (imageUrls.length > 1) {
                    mPos = currentItem - 1;
                }
            }
            if (i == mPos) {
                // 被选中状态
                Drawable originBitmapDrawable = ContextCompat.getDrawable(context,
                        R.drawable.ic_dot_selected);
                ((View) dotViewsList.get(mPos)).setBackgroundDrawable(tintDrawable(originBitmapDrawable, ContextCompat.getColor(context, R.color.colorPrimary)));
//                    ((View) dotViewsList.get(mPos))
//                            .setBackgroundResource(R.drawable.ic_dot_selected);
            } else {
                // 没被选中状态
                ((View) dotViewsList.get(i))
                        .setBackgroundResource(R.drawable.ic_dot_unselect);
            }
        }
    }

    /**
     * 填充ViewPager的页面适配器
     */
    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(imageViewsList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = imageViewsList.get(position);
            if (type == 0) {
                Glide.with(context).load(imageView.getTag(R.string.app_name) + "").into(imageView);
            } else {
                Glide.with(context).load(Integer.valueOf(imageView.getTag(R.string.app_name) + "")).into(imageView);
            }
            ((ViewPager) container).addView(imageViewsList.get(position));
            return imageViewsList.get(position);
        }

        @Override
        public int getCount() {
            return imageViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    /**
     * ViewPager的监听器 当ViewPager中页面的状态发生改变时调用
     */
    private class MyPageChangeListener implements OnPageChangeListener {

        private int lastScrollState = 0;
        private int lastPosition = 0;

        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (arg0 == 1) {
                lastScrollState = 1;
                stopPlay();
            } else if (arg0 == 2) {
                if (lastScrollState == 1) {
                    startPlay();
                }
                lastScrollState = 0;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int pos) {
            if (imageViewsList.size() > 1 && isWhile) { //多于1，才会循环跳转
                if (pos < 1) { //首位之前，跳转到末尾（N）
                    pos = imageViewsList.size() - 2; //注意这里是mList，而不是mViews
                    viewPager.setCurrentItem(pos, false);
                } else if (pos > imageViewsList.size() - 2) { //末位之后，跳转到首位（1）
                    pos = 1;
                    viewPager.setCurrentItem(pos, false); //false:不显示跳转过程的动画
                } else {
                    viewPager.setCurrentItem(pos);
                }
            }
            currentItem = pos;
            for (int i = 0; i < dotViewsList.size(); i++) {
                int mPos = pos;
                if (isWhile) {
                    mPos = 0;
                    if (imageUrls.length > 1) {
                        mPos = pos - 1;
                    }
                }
                if (i == mPos) {
                    // 被选中状态
                    Drawable originBitmapDrawable = ContextCompat.getDrawable(context,
                            R.drawable.ic_dot_selected);
                    ((View) dotViewsList.get(mPos)).setBackgroundDrawable(tintDrawable(originBitmapDrawable, ContextCompat.getColor(context, R.color.colorPrimary)));
//                    ((View) dotViewsList.get(mPos))
//                            .setBackgroundResource(R.drawable.ic_dot_selected);
                } else {
                    // 没被选中状态
                    ((View) dotViewsList.get(i))
                            .setBackgroundResource(R.drawable.ic_dot_unselect);
                }
            }
            // 判断图片是否大于一个，只有一个没有意义
            if (currentItem != 0) {
                if (listener != null && lastPosition != currentItem) {
                    if (isWhile) {
                        listener.getPosition(currentItem - 1);
                    } else {
                        listener.getPosition(currentItem);
                    }
                }
            }
            lastPosition = currentItem;
        }

    }

    /**
     * 执行轮播图切换任务
     */
    private class SlideShowTask implements Runnable {

        @Override
        public void run() {
            if (isAutoPlay) {
                // TODO Auto-generated method stub
                synchronized (viewPager) {
                    try {
                        if (imageUrls.length > 1) {
                            currentItem = (currentItem + 1) % imageUrls.length;
                        } else {
                            currentItem = (currentItem + 1) % imageViewsList.size();
                        }

                        handler.obtainMessage().sendToTarget();
                    } catch (Exception e) {
                    }
                }
            }
        }

    }

    public interface OnMyPageChangeListener {
        public void getPosition(int position);
    }

    /**
     * 对目标Drawable 进行着色
     *
     * @param drawable 目标Drawable
     * @param color    着色的颜色值
     * @return 着色处理后的Drawable
     */
    public static Drawable tintDrawable(@NonNull Drawable drawable, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

}