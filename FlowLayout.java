package com.yidian.buyer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miao on 2017/5/22.
 * 搜索自定义流式布局 自定义高和宽宽
 */

public class FlowLayout extends ViewGroup {

    /**
     * 储存所有的view 按行记录
     */
    private List<List<View>> mAllViews = new ArrayList<List<View>>();
    /**
     * 记录每一行的高度
     */
    private List<Integer> mLineHeight = new ArrayList<Integer>();
    private String TAG = "TAG";

    public FlowLayout(Context context, AttributeSet attrs,
                         int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context) {
        super(context);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 置空 view 容器 和 lineHeight 容器  重新赋值
        //因为OnMeasure方法会走两次，第一次是实例化这个对象的时候高度和宽度都是0
        //之后走了OnSizeChange()方法后 又走了一次OnMeasure，所以要把第一次加进去的数据清空。
        mAllViews.clear();
        mLineHeight.clear();
        //得到上级容器为其推荐的宽高和计算模式
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeighMode = MeasureSpec.getMode(heightMeasureSpec);
        int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int specHeighSize = MeasureSpec.getSize(heightMeasureSpec);
        // 计算出所有的 child 的 宽和高
//      measureChildren(specWidthSize, specHeighSize);
        // 记录如果是 warp_content 是设置的宽和高
        int width = 0;
        int height = 0;
        // 得到子view的个数
        int cCount = getChildCount();
        /**
         * 记录每一行的宽度，width不断取最大宽度
         */
        int lineWidth = 0;
        /**
         * 每一行的高度，累加至height
         */
        int lineHeight = 0;

        // 存储每一行所有的childView
        List<View> lineViews = new ArrayList<View>();

        for (int i = 0; i < cCount; i++) {
            // 得到每个子View
            View child = getChildAt(i);
            // 测量每个子View的宽高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 当前子view的lp
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 子view的宽和高
            int cWidth = 0;
            int cheight = 0;
            // 当前子 view 实际占的宽
            cWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            // 当前子View 实际占的高
            cheight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            lineHeight=cheight;
            // 需要换行
            if(lineWidth + cWidth > specWidthSize){
                width = Math.max(lineWidth, cWidth);// 取最大值
                lineWidth = cWidth; // 开启新行的时候重新累加width
                // 开启新行时累加 height
//              lineHeight = cheight; // 记录下一行的高度
                mAllViews.add(lineViews);
                mLineHeight.add(cheight);
                lineViews = new ArrayList<>();
                // 换行的时候把该 view 放进 集合里
                lineViews.add(child);// 这个  view(child) 是下一行的第一个view
                height += cheight; //每个View高度是一样的，直接累加
                Log.e("需要换行", "hight--" + height);
                Log.e("onMeasure", "AllViews.size()  --  > " + mAllViews.size());
            }else {
                // 不需要换行
                lineWidth += cWidth;//
                Log.e("不需要换行","hight--"+height);
                // 不需要换行时 把子View add 进集合
                lineViews.add(child);
            }

            if(i == cCount-1){
                // 如果是最后一个view
                width = Math.max(lineWidth, cWidth);
                height += cheight;
                Log.e("最后一个view","hight--"+height);
            }
        }
        // 循环结束后 把最后一行内容add进集合中
        mLineHeight.add(lineHeight); // 记录最后一行
        mAllViews.add(lineViews);
        // MeasureSpec.EXACTLY 表示设置了精确的值
        // 如果 mode 是 MeasureSpec.EXACTLY 时候，则不是 warp_content 用计算来的值，否则则用上级布局分给它的值
        setMeasuredDimension(
                specWidthMode == MeasureSpec.EXACTLY ? specWidthSize : width,
                specHeighMode == MeasureSpec.EXACTLY ? specHeighSize : height
        );
        Log.e("onMeasure", "mAllViews.size() -- > " + mAllViews.size() + "   mLineHeight.size() -- > " + mLineHeight.size() + "Height -- > "+height);
    }

    /**
     * 所有childView的位置的布局
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 当前行的最大高度
        int lineHeight = 0;
        // 存储每一行所有的childView
        List<View> lineViews = new ArrayList<View>();
        int left = 0;
        int top = 0;
        // 得到总行数
        int lineNums = mAllViews.size();
        for (int i = 0; i < lineNums; i++)
        {
            // 每一行的所有的views
            lineViews = mAllViews.get(i);
            // 当前行的最大高度
            lineHeight = mLineHeight.get(i);

            Log.e("onLayout" , "第" + i + "行 ：" + lineViews.size()+"-------lineHeight"+ lineHeight);

            // 遍历当前行所有的View
            for (int j = 0; j < lineViews.size(); j++)
            {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE)
                {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                //计算childView的left,top,right,bottom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc =lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
            }
            left = 0;
            top += lineHeight;
        }
        Log.v("onLayout", "onLayout   mAllViews.size() -- > " + mAllViews.size() + "   mLineHeight.size() -- > "+ mLineHeight.size());
    }


    /**
     * 这个一定要设置，否则会包强转错误
     * 设置它支持 marginLayoutParams
     */
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {

        return new MarginLayoutParams(getContext(),attrs);
    }

//    /** view距离父布局的边界信息*/
//    private MarginLayoutParams marginLayoutParams;
//    /** child宽度*/
//    private int childwidth;
//    /** child高度*/
//    int childheight;
//    /** 父级实际的宽度*/
//    int factwidth;
//    /** 父级实际的高度*/
//    int factheight;
//    /** 每行宽度*/
//    int linewidth;
//    /** 每行高度*/
//    int lineheight;
//    /** 用于记录上一次的行高*/
//    int lastchildheight=0;
//
//    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//    }
//
//    public FlowLayout(Context context, AttributeSet attrs) {
//        super(context, attrs,0);
//    }
//
//    public FlowLayout(Context context) {
//        super(context,null);
//    }
//    /**
//     * 根据子view来确定宽高
//     * */
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        //添加了子视图后，父级视图布局会发生改变，onMeasure方法会被执行两遍
//        factheight=0;
//        factwidth=0;
//        int widthsize=MeasureSpec.getSize(widthMeasureSpec);
//        int widthmode=MeasureSpec.getMode(widthMeasureSpec);
//        int heightsize=MeasureSpec.getSize(heightMeasureSpec);
//        int heightmode=MeasureSpec.getMode(heightMeasureSpec);
//        int childcount=getChildCount();
//        for(int i=0;i<childcount;i++){
//            View child=getChildAt(i);
//            measureChild(child, widthMeasureSpec, heightMeasureSpec);
//            marginLayoutParams=(MarginLayoutParams) child.getLayoutParams();
//            childwidth=child.getMeasuredWidth()+marginLayoutParams.leftMargin+marginLayoutParams.rightMargin;
//            childheight=child.getMeasuredHeight()+marginLayoutParams.topMargin+marginLayoutParams.bottomMargin;
//            //如果child的宽度和加起来大于了父级宽度，则计算最大值，并作为父级实际宽度
//            if(linewidth+childwidth>widthsize){
//                //计算父级宽度
//                factwidth=Math.max(widthsize,linewidth);
//                //计算父级高度
//                factheight+=lastchildheight;
//                //重置行宽
//                linewidth=0;
//                //重置行高
//                lastchildheight=0;
//                lineheight=0;
//            }
//            linewidth+=childwidth;
//            //取最大行高(用于应对特殊字体大小)
//            lineheight=Math.max(lastchildheight,childheight);
//            lastchildheight=lineheight;
//            //特殊情况，处理最后一个child（有可能该行只有一个view,也可能最后一个view刚好处于最后一行最后位置）
//            if(i==childcount-1){
//                factwidth=Math.max(widthsize,linewidth);
//                factheight+=lastchildheight;
//                lastchildheight=0;
//                lineheight=0;
//                linewidth=0;
//            }
//        }
//        //把测量结果设置为父级宽高
//        setMeasuredDimension(widthmode==MeasureSpec.EXACTLY?widthsize:factwidth,
//                heightmode==MeasureSpec.EXACTLY?heightsize:factheight);
//    }
//    /** 每行view的集合*/
//    private List<List<View>> AllChildView=new ArrayList<List<View>>();
//    /** 每行高度的集合*/
//    private List<Integer> LineHeight=new ArrayList<Integer>();
//    /**
//     * 排版 (横向排列)
//     * */
//    @SuppressLint("DrawAllocation")
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        factwidth=getMeasuredWidth();
//        AllChildView.clear();
//        LineHeight.clear();
//        List<View> linelist=new ArrayList<View>();
//        //计算每行可以放view的个数，并放进集合
//        int childcount=getChildCount();
//        for(int i=0;i<childcount;i++){
//            View view=getChildAt(i);
//            marginLayoutParams=(MarginLayoutParams) view.getLayoutParams();
//            int childwidth=view.getMeasuredWidth()+marginLayoutParams.leftMargin+marginLayoutParams.rightMargin;
//            int childheight=view.getMeasuredHeight()+marginLayoutParams.topMargin+marginLayoutParams.bottomMargin;
//            //每行子view加起来的宽度大于父级宽度 就把该行子view集合放进所有行的集合里
//            if(linewidth+childwidth>=factwidth){
//                LineHeight.add(lastchildheight);//行高集合
//                AllChildView.add(linelist);//行数集合
//                //重置
//                linewidth=0;
//                lastchildheight=0;
//                //重新创建一个集合
//                linelist=new ArrayList<View>();
//            }
//            //取每行的最大高度
//            lineheight=Math.max(childheight,lastchildheight);
//            lastchildheight=lineheight;
//            linewidth+=childwidth;
//            //每行的view集合
//            linelist.add(view);
//            //如果最后一行没有大于父级宽度，需要特殊处理
//            if(i==childcount-1){
//                LineHeight.add(lastchildheight);//行高集合
//                AllChildView.add(linelist);//行数集合
//                lastchildheight=0;
//                linewidth=0;
//            }
//        }
//        int left=0;
//        int top=0;
//        //设置子view的位置
//        for(int w=0;w<AllChildView.size();w++){//总共多少行
//            linelist=AllChildView.get(w);
//            lineheight=LineHeight.get(w);
//            for(int m=0;m<linelist.size();m++){//每行排版
//                View childview=linelist.get(m);
//                //隐藏状态的子view不参与排版
//                if(childview.getVisibility()==View.GONE){
//                    continue;
//                }
//                marginLayoutParams=(MarginLayoutParams) childview.getLayoutParams();
//                int cleft=left+marginLayoutParams.leftMargin;
//                int ctop=top+marginLayoutParams.topMargin+(lineheight/2-childview.getHeight()/2);
//                int cright=cleft+childview.getMeasuredWidth();
//                int cbottom=ctop+childview.getMeasuredHeight();
//                childview.layout(cleft, ctop, cright, cbottom);
//                left+=childview.getMeasuredWidth()+marginLayoutParams.leftMargin+marginLayoutParams.rightMargin;
//            }
//            //每行排完之后重新设置属性
//            left=0;
//            top+=lineheight;
//        }
//    }
//  @Override
//  public LayoutParams generateLayoutParams(AttributeSet attrs) {
//      return new MarginLayoutParams(getContext(), attrs);
//  }

    ////////////////////////////////////////////////////////////////////////////////////////



//    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//    }
//
//    public FlowLayout(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public FlowLayout(Context context) {
//        super(context);
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
//        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
//        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
//        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
//
//        // warp_content
//        int width = 0, height = 0, lineWidth = 0, lineHeight = 0;
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            measureChild(child, widthMeasureSpec, heightMeasureSpec);
//            MarginLayoutParams params = (MarginLayoutParams) child
//                    .getLayoutParams();
//            int childWidth = child.getMeasuredWidth() + params.leftMargin
//                    + params.rightMargin;
//            int childHeight = child.getMeasuredHeight() + params.topMargin
//                    + params.bottomMargin;
//            if (childWidth + lineWidth > sizeWidth - getPaddingLeft()
//                    - getPaddingRight()) {
//                // 对比得到最大宽度
//                width = Math.max(lineWidth, width);
//                // 重置lineWidth
//                lineWidth = childWidth;
//                // 记录行高
//                height += lineHeight;
//                lineHeight = childHeight;
//            } else {
//                lineWidth += childWidth;
//                lineHeight = Math.max(lineHeight, childHeight);
//            }
//            if (i == getChildCount() - 1) {
//                width = Math.max(lineWidth, width);
//                height += lineHeight;
//            }
//        }
//        setMeasuredDimension(modeWidth == MeasureSpec.EXACTLY ? sizeWidth
//                        : width + getPaddingLeft() + getPaddingRight(),
//                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height
//                        + getPaddingTop() + getPaddingBottom());
//    }
//
//    private List<List<View>> mAllViews = new ArrayList<List<View>>();
//    // 每一行的高度
//    private List<Integer> mLineHeight = new ArrayList<Integer>();
//    // 每一行距离左边的距离
//    private List<Integer> mLineMarginLeft = new ArrayList<Integer>();
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        mAllViews.clear();
//        mLineHeight.clear();
//        int width = getMeasuredWidth();
//        int lineWidth = 0, lineHeight = 0;
//        List<View> lineViews = new ArrayList<View>();
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            MarginLayoutParams params = (MarginLayoutParams) child
//                    .getLayoutParams();
//            int childWidth = child.getMeasuredWidth() + params.leftMargin
//                    + params.rightMargin;
//            int childHeight = child.getMeasuredHeight() + params.topMargin
//                    + params.bottomMargin;
//            if (childWidth + lineWidth > width - getPaddingLeft()
//                    - getPaddingRight()) {
//                // 计算每行距离左边距离
//                int lineLeftMargin = (width - lineWidth) / 2;
//                mLineMarginLeft.add(lineLeftMargin);
//                // 行高
//                mLineHeight.add(lineHeight);
//                mAllViews.add(lineViews);
//                lineWidth = 0;
//                lineHeight = childHeight;
//                lineViews = new ArrayList<View>();
//            }
//            lineWidth += childWidth;
//            lineHeight = Math.max(lineHeight, childHeight);
//            lineViews.add(child);
//        }
//        /** 处理最后一行 **/
//        // 计算每行距离左边距离
//        int lineLeftMargin = (width - lineWidth) / 2;
//        mLineMarginLeft.add(lineLeftMargin);
//        // 行高
//        mLineHeight.add(lineHeight);
//        mAllViews.add(lineViews);
//
//        /** 设置子View的位置 **/
//        int left = getPaddingLeft(), top = getPaddingTop();
//        for (int i = 0; i < mAllViews.size(); i++) {
//            lineViews = mAllViews.get(i);
//            lineHeight = mLineHeight.get(i);
//            // 居中显示的左边距
//            left += mLineMarginLeft.get(i);
//            for (int j = 0; j < lineViews.size(); j++) {
//                View child = lineViews.get(j);
//                if (child.getVisibility() == View.GONE)
//                    continue;
//                MarginLayoutParams params = (MarginLayoutParams) child
//                        .getLayoutParams();
//                int lc = left + params.leftMargin;
//                int tc = top + params.topMargin;
//                int rc = lc + child.getMeasuredWidth();
//                int bc = tc + child.getMeasuredHeight();
//                child.layout(lc, tc, rc, bc);
//                left += child.getMeasuredWidth() + params.leftMargin
//                        + params.rightMargin;
//            }
//            left = getPaddingLeft();
//            top += lineHeight;
//        }
//    }
//
//    @Override
//    public LayoutParams generateLayoutParams(AttributeSet attrs) {
//        return new MarginLayoutParams(getContext(), attrs);
//    }
}
