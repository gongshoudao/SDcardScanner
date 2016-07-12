package com.androidcycle.sdcardscanner.storage;

import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.androidcycle.sdcardscanner.R;
import com.androidcycle.sdcardscanner.common.utils.StorageUtils;

import java.util.ArrayList;

public class StoragePathRecyclerAdapter extends RecyclerView.Adapter {

    private final StorageViewHoder.StorageOperatorInterface mOperatorInterface;
    private ArrayList<StorageBean> mDataSet;

    public StoragePathRecyclerAdapter(StorageViewHoder.StorageOperatorInterface operatorInterface) {
        mOperatorInterface = operatorInterface;
    }

    public void refreshData(ArrayList<StorageBean> pDataSet) {
        this.mDataSet = pDataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_item_ll, parent, false);
        return new StorageViewHoder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((StorageViewHoder) holder).setStorageData(mDataSet.get(position), mOperatorInterface);
    }

    @Override
    public int getItemCount() {
        return mDataSet == null || mDataSet.size() == 0 ? 0 : mDataSet.size();
    }

    public static class StorageViewHoder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView typeTv;
        private StorageOperatorInterface operatorInterface;
        private String mPath;
        private final TextView mNoticeTv;
        private View mNoticeView;
        private boolean mShowErrorAfterAttach;
        private ErrorPopup mErrorPopup;

        public interface StorageOperatorInterface {
            void onOpenExplorer(String url);

            void onWriteTest(String url);

            void onDeleteTest(String url);
        }

        private final TextView mPathTv;
        private final TextView mRemoveableTv;
        private final TextView totalSpaceTv;
        private final TextView availableSpaceTv;
        private final AppCompatButton openExplorer;
        private final AppCompatButton wrTest;
        private final AppCompatButton deleteTest;
        private final TextView mountedTv;

        public StorageViewHoder(View itemView) {
            super(itemView);
            mPathTv = (TextView) itemView.findViewById(R.id.storage_item_path_tv);
            typeTv = (TextView) itemView.findViewById(R.id.storage_item_type_tv);
            mRemoveableTv = (TextView) itemView.findViewById(R.id.storage_item_removeable_tv);
            mountedTv = (TextView) itemView.findViewById(R.id.storage_item_mounted_tv);
            totalSpaceTv = (TextView) itemView.findViewById(R.id.storage_item_space_total_tv);
            availableSpaceTv = (TextView) itemView.findViewById(R.id.storage_item_space_aviliable_tv);
            openExplorer = (AppCompatButton) itemView.findViewById(R.id.storage_item_open_explorer_btn);
            wrTest = (AppCompatButton) itemView.findViewById(R.id.storage_item_write_test_btn);
            deleteTest = (AppCompatButton) itemView.findViewById(R.id.storage_item_delete_test_file_btn);
            mNoticeTv = (TextView) itemView.findViewById(R.id.storage_item_notice);
        }

        public void setStorageData(StorageBean storageData, StorageOperatorInterface operatorInterface) {

            mPath = storageData.getPath();
            mPathTv.setText("路径：" + mPath);

            boolean removable = storageData.getRemovable();
            mRemoveableTv.setText(removable ? "可移除" : "不可移除");

            if (mPath.contains("usb")) {
                typeTv.setText("USB");
            } else {
                typeTv.setText(removable ? "SD card" : "Internal Storage");
            }
            int colorRes = removable ? R.color.color_scheme_1_2 : R.color.color_scheme_1_4;
            final int color = mRemoveableTv.getContext().getResources().getColor(colorRes);
            mRemoveableTv.setTextColor(color);
            typeTv.setTextColor(color);


            final boolean mounted = storageData.getMounted().equalsIgnoreCase("mounted");
            mountedTv.setText(mounted ? "已挂载" : "未挂载");
            int colorMount = mounted ? R.color.color_scheme_1_1 : R.color.color_scheme_1_4;
            mountedTv.setTextColor(mountedTv.getContext().getResources().getColor(colorMount));


            long totalSize = storageData.getTotalSize();
            String totalSpaceStr = StorageUtils.fmtSpace(totalSize);
            totalSpaceTv.setText("总空间：" + totalSize + "(" + totalSpaceStr + ")");

            long availableSize = storageData.getAvailableSize();
            String availableSizeStr = StorageUtils.fmtSpace(availableSize);
            availableSpaceTv.setText("可用空间：" + availableSize + "(" + availableSizeStr + ")");

            if (removable && !mounted) {
                mNoticeTv.setVisibility(View.GONE);
            } else {
                mNoticeTv.setVisibility(View.VISIBLE);
                mNoticeTv.setText(String.format(mNoticeTv.getContext().getString(R.string.test_write_notice_format), mPath));
            }
            mNoticeView = itemView.findViewById(R.id.storage_item_space_notice);
            mNoticeView.setOnClickListener(this);
            this.operatorInterface = operatorInterface;
            openExplorer.setOnClickListener(this);
            wrTest.setOnClickListener(this);
            deleteTest.setOnClickListener(this);

            openExplorer.setEnabled(mounted);
            wrTest.setEnabled(mounted);
            deleteTest.setEnabled(mounted);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.storage_item_open_explorer_btn:
                    if (operatorInterface != null) {
                        operatorInterface.onOpenExplorer(mPath);
                    }
                    break;
                case R.id.storage_item_delete_test_file_btn:
                    if (operatorInterface != null) {
                        operatorInterface.onDeleteTest(mPath);
                    }
                    break;
                case R.id.storage_item_write_test_btn:
                    if (operatorInterface != null) {
                        operatorInterface.onWriteTest(mPath);
                    }
                    break;
                case R.id.storage_item_space_notice:
//                    showPop();
                    break;
                default:
                    break;
            }
        }

        private void showPop() {
            if (mNoticeView.getWindowToken() == null) {
                mShowErrorAfterAttach = true;
                return;
            }

            if (mErrorPopup == null) {
                LayoutInflater inflater = LayoutInflater.from(mNoticeView.getContext());
                final TextView err = (TextView) inflater.inflate(R.layout.textview_hint, null);

                final float scale = mNoticeView.getResources().getDisplayMetrics().density;
                mErrorPopup = new ErrorPopup(err, (int) (200 * scale + 0.5f), (int) (50 * scale + 0.5f));
                mErrorPopup.setFocusable(false);
                // The user is entering text, so the input method is needed.  We
                // don't want the popup to be displayed on top of it.
                mErrorPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            }

            TextView tv = (TextView) mErrorPopup.getContentView();
            String mError = "不同软件的计算方式差异，可能会导致计算结果出现偏差";
            chooseSize(mErrorPopup, mError, tv);
            tv.setText(mError);

            mErrorPopup.showAsDropDown(mNoticeView, getErrorX(), getErrorY());
            mErrorPopup.fixDirection(mErrorPopup.isAboveAnchor());
        }

        private int getErrorY() {
//            final int compoundPaddingTop = mNoticeView.getPaddingTop();
//            int vspace = mNoticeView.getBottom() - mNoticeView.getTop() -
//                    mNoticeView.getCompoundPaddingBottom() - compoundPaddingTop;

            final float scale = mNoticeView.getResources().getDisplayMetrics().density;
            return mNoticeView.getHeight() - (int) (2 * scale + 0.5f);
        }

        private int getErrorX() {
            final float scale = mNoticeView.getResources().getDisplayMetrics().density;
            int offset = -(mNoticeView.getWidth()) / 2 + (int) (25 * scale + 0.5f);
            int errorX = mNoticeView.getWidth() - mErrorPopup.getWidth() -
                    mNoticeView.getPaddingRight() + offset;
            return errorX;
        }

        private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
            int wid = tv.getPaddingLeft() + tv.getPaddingRight();
            int ht = tv.getPaddingTop() + tv.getPaddingBottom();

            int defaultWidthInPixels = mNoticeView.getResources().getDimensionPixelSize(R.dimen.textview_error_popup_default_width);
            Layout l = new StaticLayout(text, tv.getPaint(), defaultWidthInPixels,
                    Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
            float max = 0;
            for (int i = 0; i < l.getLineCount(); i++) {
                max = Math.max(max, l.getLineWidth(i));
            }

        /*
         * Now set the popup size to be big enough for the text plus the border capped
         * to DEFAULT_MAX_POPUP_WIDTH
         */
            pop.setWidth(wid + (int) Math.ceil(max));
            pop.setHeight(ht + l.getHeight());
        }
    }

    private static class ErrorPopup extends PopupWindow {
        private boolean mAbove = false;
        private final TextView mView;
        private int mPopupInlineErrorAboveBackgroundId = 0;

        ErrorPopup(TextView v, int width, int height) {
            super(v, width, height);
            mView = v;
            // Make sure the TextView has a background set as it will be used the first time it is
            // shown and positioned. Initialized with below background, which should have
            // dimensions identical to the above version for this to work (and is more likely).
            mView.setBackgroundResource(R.mipmap.popup_background_mtrl_mult);
        }

        void fixDirection(boolean above) {
            mAbove = above;

           /* if (above) {
                mPopupInlineErrorAboveBackgroundId =
                        getResourceId(mPopupInlineErrorAboveBackgroundId,
                                com.android.internal.R.styleable.Theme_errorMessageAboveBackground);
            }*/

            mView.setBackgroundResource(R.mipmap.popup_background_mtrl_mult);
        }

        private int getResourceId(int currentId, int index) {
            if (currentId == 0) {
                TypedArray styledAttributes = mView.getContext().obtainStyledAttributes(android.support.v7.appcompat.R.styleable.AppCompatTheme);
                currentId = styledAttributes.getResourceId(index, 0);
                styledAttributes.recycle();
            }
            return currentId;
        }

        @Override
        public void update(int x, int y, int w, int h, boolean force) {
            super.update(x, y, w, h, force);

            boolean above = isAboveAnchor();
            if (above != mAbove) {
                fixDirection(above);
            }
        }
    }
}
