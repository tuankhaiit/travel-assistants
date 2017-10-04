package com.tuankhai.travelassistants.module.floatingsearchview.main;

/**
 * Created by tuank on 03/09/2017.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tuankhai.travelassistants.R;
import com.tuankhai.travelassistants.module.floatingsearchview.utils.Util;
import com.tuankhai.travelassistants.module.floatingsearchview.utils.adapter.TextWatcherAdapter;
import com.tuankhai.travelassistants.module.floatingsearchview.utils.view.MenuView;
import com.tuankhai.travelassistants.module.floatingsearchview.utils.view.SearchInputView;
import com.tuankhai.travelassistants.module.floatingsearchview.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * A search UI widget that implements a floating search box also called persistent
 * search.
 */
public class FloatingSearchView extends FrameLayout {

    private final static String TAG = FloatingSearchView.class.getSimpleName();
    //The CardView's top or bottom height used for its shadow
    private final static int CARD_VIEW_TOP_BOTTOM_SHADOW_HEIGHT = 3;
    //The CardView's (default) corner radius height
    private final static int CARD_VIEW_CORNERS_HEIGHT = 2;

    private final static long CLEAR_BTN_FADE_ANIM_DURATION = 500;
    private final static int CLEAR_BTN_WIDTH_DP = 48;

    private final static int LEFT_MENU_WIDTH_AND_MARGIN_START_DP = 52;

    private final static float MENU_BUTTON_PROGRESS_ARROW = 1.0f;
    private final static float MENU_BUTTON_PROGRESS_HAMBURGER = 0.0f;

    private final static int BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED = 150;
    private final static int BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED = 0;
    private final static int BACKGROUND_FADE_ANIM_DURATION = 250;

    private final static int MENU_ICON_ANIM_DURATION = 250;

    public final static int LEFT_ACTION_MODE_SHOW_HAMBURGER = 1;
    public final static int LEFT_ACTION_MODE_SHOW_SEARCH = 2;
    public final static int LEFT_ACTION_MODE_SHOW_HOME = 3;
    public final static int LEFT_ACTION_MODE_NO_LEFT_ACTION = 4;
    private final static int LEFT_ACTION_MODE_NOT_SET = -1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LEFT_ACTION_MODE_SHOW_HAMBURGER, LEFT_ACTION_MODE_SHOW_SEARCH,
            LEFT_ACTION_MODE_SHOW_HOME, LEFT_ACTION_MODE_NO_LEFT_ACTION, LEFT_ACTION_MODE_NOT_SET})
    public @interface LeftActionMode {
    }

    @LeftActionMode
    private final static int ATTRS_SEARCH_BAR_LEFT_ACTION_MODE_DEFAULT = LEFT_ACTION_MODE_NO_LEFT_ACTION;
    private final static boolean ATTRS_DISMISS_ON_KEYBOARD_DISMISS_DEFAULT = false;
    private final static boolean ATTRS_SEARCH_BAR_SHOW_SEARCH_KEY_DEFAULT = true;
    private final static int ATTRS_QUERY_TEXT_SIZE_SP_DEFAULT = 18;
    private final static boolean ATTRS_SHOW_DIM_BACKGROUND_DEFAULT = true;
    private final static int ATTRS_SEARCH_BAR_MARGIN_DEFAULT = 0;
    private final static boolean ATTRS_DISMISS_FOCUS_ON_ITEM_SELECTION_DEFAULT = false;

    private Activity mHostActivity;

    private View mMainLayout;
    private Drawable mBackgroundDrawable;
    private boolean mDimBackground;
    private boolean mDismissOnOutsideTouch = true;
    private boolean mIsFocused;
    private OnFocusChangeListener mFocusChangeListener;
    private boolean mDismissFocusOnItemSelection = ATTRS_DISMISS_FOCUS_ON_ITEM_SELECTION_DEFAULT;

    private CardView mQuerySection;
    private OnSearchListener mSearchListener;
    public SearchInputView mSearchInput;
    private int mQueryTextSize;
    private boolean mCloseSearchOnSofteKeyboardDismiss;
    private String mTitleText;
    private boolean mIsTitleSet;
    private int mSearchInputTextColor = -1;
    private int mSearchInputHintColor = -1;
    private View mSearchInputParent;
    private String mOldQuery = "";
    private OnQueryChangeListener mQueryListener;
    private ImageView mLeftAction;
    private OnLeftMenuClickListener mOnMenuClickListener;
    private OnHomeActionClickListener mOnHomeActionClickListener;
    private ProgressBar mSearchProgress;
    private DrawerArrowDrawable mMenuBtnDrawable;
    private Drawable mIconBackArrow;
    private Drawable mIconSearch;
    @LeftActionMode
    int mLeftActionMode = LEFT_ACTION_MODE_NOT_SET;
    private int mLeftActionIconColor;
    private String mSearchHint;
    private boolean mShowSearchKey;
    private boolean mMenuOpen = false;
    private MenuView mMenuView;
    private int mMenuId = -1;
    private int mActionMenuItemColor;
    private int mOverflowIconColor;
    private OnMenuItemClickListener mActionMenuItemListener;
    private ImageView mClearButton;
    private int mClearBtnColor;
    private Drawable mIconClear;
    private int mBackgroundColor;
    private boolean mSkipQueryFocusChangeEvent;
    private boolean mSkipTextChangeEvent;
    private View.OnClickListener mLeftMenuClickListener;

    private View mDivider;
    private int mDividerColor;
    private onBackSearchButtonListener mOnBackSearchButtonListener;

    public void setOnBackSearchButtonListener(onBackSearchButtonListener listener) {
        this.mOnBackSearchButtonListener = listener;
    }

    private OnClearSearchActionListener mOnClearSearchActionListener;

    /**
     * Interface for implementing a listener to listen
     * to state changes in the query text.
     */
    public interface OnQueryChangeListener {

        /**
         * Called when the query has changed. It will
         * be invoked when one or more characters in the
         * query was changed.
         *
         * @param oldQuery the previous query
         * @param newQuery the new query
         */
        void onSearchTextChanged(String oldQuery, String newQuery);
    }

    /**
     * Interface for implementing a listener to listen
     * to when the current search has completed.
     */
    public interface OnSearchListener {
        /**
         * Called when the current search has completed
         * as a result of pressing search key in the keyboard.
         * <p/>
         * Note: This will only get called if
         * {@link FloatingSearchView#setShowSearchKey(boolean)}} is set to true.
         *
         * @param currentQuery the text that is currently set in the query TextView
         */
        void onSearchAction(String currentQuery);
    }

    /**
     * Interface for implementing a callback to be
     * invoked when the left menu (navigation menu) is
     * clicked.
     * <p/>
     * Note: This is only relevant when leftActionMode is
     * set to {@value #LEFT_ACTION_MODE_SHOW_HAMBURGER}
     */
    public interface OnLeftMenuClickListener {

        /**
         * Called when the menu button was
         * clicked and the menu's state is now opened.
         */
        void onMenuOpened();

        /**
         * Called when the back button was
         * clicked and the menu's state is now closed.
         */
        void onMenuClosed();
    }

    /**
     * Interface for implementing a callback to be
     * invoked when the home action button (the back arrow)
     * is clicked.
     * <p/>
     * Note: This is only relevant when leftActionMode is
     * set to {@value #LEFT_ACTION_MODE_SHOW_HOME}
     */
    public interface OnHomeActionClickListener {

        /**
         * Called when the home button was
         * clicked.
         */
        void onHomeClicked();
    }

    /**
     * Interface for implementing a listener to listen
     * when an item in the action (the item can be presented as an action
     * ,or as a menu item in the overflow menu) menu has been selected.
     */
    public interface OnMenuItemClickListener {

        /**
         * Called when a menu item in has been
         * selected.
         *
         * @param item the selected menu item.
         */
        void onActionMenuItemSelected(MenuItem item);
    }

    /**
     * Interface for implementing a listener to listen
     * to for focus state changes.
     */
    public interface OnFocusChangeListener {

        /**
         * Called when the search bar has gained focus
         * and listeners are now active.
         */
        void onFocus();

        /**
         * Called when the search bar has lost focus
         * and listeners are no more active.
         */
        void onFocusCleared();
    }

    /**
     * Interface for implementing a callback to be
     * invoked when the clear search text action button
     * (the x to the right of the text) is clicked.
     */
    public interface OnClearSearchActionListener {

        /**
         * Called when the clear search text button
         * was clicked.
         */
        void onClearSearchClicked();
    }

    public FloatingSearchView(Context context) {
        this(context, null);
    }

    public FloatingSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public boolean setSearchFocused(final boolean focused) {
        boolean updatedToNotFocused = !focused && this.mIsFocused;
        return updatedToNotFocused;
    }

    private void init(AttributeSet attrs) {

        mHostActivity = Util.getHostActivity(getContext());

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainLayout = inflate(getContext(), R.layout.floating_search_layout, this);
        mBackgroundDrawable = new ColorDrawable(Color.BLACK);

        mQuerySection = findViewById(R.id.search_query_section);
        mClearButton = findViewById(R.id.clear_btn);
        mSearchInput = findViewById(R.id.search_bar_text);
        mSearchInputParent = findViewById(R.id.search_input_parent);
        mLeftAction = findViewById(R.id.left_action);
        mSearchProgress = findViewById(R.id.search_bar_search_progress);
        initDrawables();
        mClearButton.setImageDrawable(mIconClear);
        mMenuView = findViewById(R.id.menu_view);

        mDivider = findViewById(R.id.divider);

        setupViews(attrs);
    }

    private void initDrawables() {
        mMenuBtnDrawable = new DrawerArrowDrawable(getContext());
        mIconClear = Util.getWrappedDrawable(getContext(), R.drawable.ic_clear_black_24dp);
        mIconBackArrow = Util.getWrappedDrawable(getContext(), R.drawable.ic_arrow_back_black_24dp);
        mIconSearch = Util.getWrappedDrawable(getContext(), R.drawable.ic_search_black_24dp);
    }

    private void setupViews(AttributeSet attrs) {

        if (attrs != null) {
            applyXmlAttributes(attrs);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mBackgroundDrawable);
        } else {
            setBackgroundDrawable(mBackgroundDrawable);
        }

        setupQueryBar();
    }

    private void applyXmlAttributes(AttributeSet attrs) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingSearchView);

        try {

            int searchBarWidth = a.getDimensionPixelSize(
                    R.styleable.FloatingSearchView_floatingSearch_searchBarWidth,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mQuerySection.getLayoutParams().width = searchBarWidth;
            mDivider.getLayoutParams().width = searchBarWidth;
            int searchBarLeftMargin = a.getDimensionPixelSize(
                    R.styleable.FloatingSearchView_floatingSearch_searchBarMarginLeft,
                    ATTRS_SEARCH_BAR_MARGIN_DEFAULT);
            int searchBarTopMargin = a.getDimensionPixelSize(
                    R.styleable.FloatingSearchView_floatingSearch_searchBarMarginTop,
                    ATTRS_SEARCH_BAR_MARGIN_DEFAULT);
            int searchBarRightMargin = a.getDimensionPixelSize(
                    R.styleable.FloatingSearchView_floatingSearch_searchBarMarginRight,
                    ATTRS_SEARCH_BAR_MARGIN_DEFAULT);
            FrameLayout.LayoutParams querySectionLP = (LayoutParams) mQuerySection.getLayoutParams();
            LayoutParams dividerLP = (LayoutParams) mDivider.getLayoutParams();
            int cardPadding = Util.dpToPx(CARD_VIEW_TOP_BOTTOM_SHADOW_HEIGHT);
            querySectionLP.setMargins(searchBarLeftMargin, searchBarTopMargin,
                    searchBarRightMargin, 0);
            dividerLP.setMargins(searchBarLeftMargin + cardPadding, 0,
                    searchBarRightMargin + cardPadding,
                    ((MarginLayoutParams) mDivider.getLayoutParams()).bottomMargin);
            mQuerySection.setLayoutParams(querySectionLP);
            mDivider.setLayoutParams(dividerLP);

            setQueryTextSize(a.getDimensionPixelSize(R.styleable.FloatingSearchView_floatingSearch_searchInputTextSize,
                    ATTRS_QUERY_TEXT_SIZE_SP_DEFAULT));
            setSearchHint(a.getString(R.styleable.FloatingSearchView_floatingSearch_searchHint));
            setShowSearchKey(a.getBoolean(R.styleable.FloatingSearchView_floatingSearch_showSearchKey,
                    ATTRS_SEARCH_BAR_SHOW_SEARCH_KEY_DEFAULT));
            setCloseSearchOnKeyboardDismiss(a.getBoolean(R.styleable.FloatingSearchView_floatingSearch_close_search_on_keyboard_dismiss,
                    ATTRS_DISMISS_ON_KEYBOARD_DISMISS_DEFAULT));
            //noinspection ResourceType
            mLeftActionMode = a.getInt(R.styleable.FloatingSearchView_floatingSearch_leftActionMode,
                    ATTRS_SEARCH_BAR_LEFT_ACTION_MODE_DEFAULT);
            if (a.hasValue(R.styleable.FloatingSearchView_floatingSearch_menu)) {
                mMenuId = a.getResourceId(R.styleable.FloatingSearchView_floatingSearch_menu, -1);
            }
            setDimBackground(a.getBoolean(R.styleable.FloatingSearchView_floatingSearch_dimBackground,
                    ATTRS_SHOW_DIM_BACKGROUND_DEFAULT));
            setBackgroundColor(a.getColor(R.styleable.FloatingSearchView_floatingSearch_backgroundColor
                    , Util.getColor(getContext(), R.color.background)));
            setLeftActionIconColor(a.getColor(R.styleable.FloatingSearchView_floatingSearch_leftActionColor
                    , Util.getColor(getContext(), R.color.left_action_icon)));
            setActionMenuOverflowColor(a.getColor(R.styleable.FloatingSearchView_floatingSearch_actionMenuOverflowColor
                    , Util.getColor(getContext(), R.color.overflow_icon_color)));
            setMenuItemIconColor(a.getColor(R.styleable.FloatingSearchView_floatingSearch_menuItemIconColor
                    , Util.getColor(getContext(), R.color.menu_icon_color)));
            setClearBtnColor(a.getColor(R.styleable.FloatingSearchView_floatingSearch_clearBtnColor
                    , Util.getColor(getContext(), R.color.clear_btn_color)));
            int viewTextColor = a.getColor(R.styleable.FloatingSearchView_floatingSearch_viewTextColor
                    , Util.getColor(getContext(), R.color.dark_gray));
            setQueryTextColor(a.getColor(R.styleable.FloatingSearchView_floatingSearch_viewSearchInputTextColor
                    , viewTextColor));
            setHintTextColor(a.getColor(R.styleable.FloatingSearchView_floatingSearch_hintTextColor
                    , Util.getColor(getContext(), R.color.hint_color)));
        } finally {
            a.recycle();
        }
    }

    private void setupQueryBar() {

        mSearchInput.setTextColor(mSearchInputTextColor);
        mSearchInput.setHintTextColor(mSearchInputHintColor);

        if (!isInEditMode() && mHostActivity != null) {
            mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        ViewTreeObserver vto = mQuerySection.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Util.removeGlobalLayoutObserver(mQuerySection, this);

                inflateOverflowMenu(mMenuId);
            }
        });

        mMenuView.setMenuCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {

                if (mActionMenuItemListener != null) {
                    mActionMenuItemListener.onActionMenuItemSelected(item);
                }

                //todo check if we should care about this return or not
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }

        });

        mMenuView.setOnVisibleWidthChanged(new MenuView.OnVisibleWidthChangedListener() {
            @Override
            public void onItemsMenuVisibleWidthChanged(int newVisibleWidth) {
                handleOnVisibleMenuItemsWidthChanged(newVisibleWidth);
            }
        });

        mMenuView.setActionIconColor(mActionMenuItemColor);
        mMenuView.setOverflowColor(mOverflowIconColor);

        mClearButton.setVisibility(View.INVISIBLE);
        mClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchInput.setText("");
                if (mOnClearSearchActionListener != null) {
                    mOnClearSearchActionListener.onClearSearchClicked();
                }
                Util.showSoftKeyboard(getContext(), mSearchInput);
            }

        });

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchInput.addTextChangedListener(new TextWatcherAdapter() {

            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                //todo investigate why this is called twice when pressing back on the keyboard

                if (mSkipTextChangeEvent || !mIsFocused) {
                    mSkipTextChangeEvent = false;
                } else {
                    if (mSearchInput.getText().toString().length() != 0 &&
                            mClearButton.getVisibility() == View.INVISIBLE) {
                        mClearButton.setAlpha(0.0f);
                        mClearButton.setVisibility(View.VISIBLE);
                        ViewCompat.animate(mClearButton).alpha(1.0f).setDuration(CLEAR_BTN_FADE_ANIM_DURATION).start();
                    } else if (mSearchInput.getText().toString().length() == 0) {
                        mClearButton.setVisibility(View.INVISIBLE);
                    }

                    if (mQueryListener != null && mIsFocused && !mOldQuery.equals(mSearchInput.getText().toString())) {
                        mQueryListener.onSearchTextChanged(mOldQuery, mSearchInput.getText().toString());
                    }

                }

                mOldQuery = mSearchInput.getText().toString();
            }

        });

        mSearchInput.setOnFocusChangeListener(new TextView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (mSkipQueryFocusChangeEvent) {
                    mSkipQueryFocusChangeEvent = false;
                } else if (hasFocus != mIsFocused) {
                    setSearchFocusedInternal(hasFocus);
                }
            }
        });

        mSearchInput.setOnKeyboardDismissedListener(new SearchInputView.OnKeyboardDismissedListener() {
            @Override
            public void onKeyboardDismissed() {
                if (mCloseSearchOnSofteKeyboardDismiss) {
                    setSearchFocusedInternal(false);
                }
            }
        });

        mSearchInput.setOnSearchKeyListener(new SearchInputView.OnKeyboardSearchKeyClickListener() {
            @Override
            public void onSearchKeyClicked() {
                if (mSearchListener != null) {
                    mSearchListener.onSearchAction(getQuery());
                }
                mSkipTextChangeEvent = true;
                if (mIsTitleSet) {
                    setSearchBarTitle(getQuery());
                } else {
                    setSearchText(getQuery());
                }
                //setSearchFocusedInternal(false);
//                mMainLayout.requestFocus();
                if (mDimBackground) {
                    fadeOutBackground();
                }
//                handleOnVisibleMenuItemsWidthChanged(0);//this must be called before  mMenuView.hideIfRoomItems(...)
//                mMenuView.showIfRoomItems(true);
//                transitionOutLeftSection(true);
//                mClearButton.setVisibility(View.GONE);
                if (mHostActivity != null) {
                    Util.closeSoftKeyboard(mHostActivity);
                }
                if (mIsTitleSet) {
                    mSkipTextChangeEvent = true;
                    mSearchInput.setText(mTitleText);
                }
                mSearchInput.setLongClickable(false);
//                if (mFocusChangeListener != null) {
//                    mFocusChangeListener.onFocusCleared();
//                }
                mSearchInput.setSelection(mSearchInput.length());
            }
        });

        mLeftAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSearchBarFocused()) {
                    setSearchFocusedInternal(false);
                } else {
                    switch (mLeftActionMode) {
                        case LEFT_ACTION_MODE_SHOW_HAMBURGER:
                            if (mLeftMenuClickListener != null) {
                                mLeftMenuClickListener.onClick(mLeftAction);
                            } else {
                                toggleLeftMenu();
                            }
                            break;
                        case LEFT_ACTION_MODE_SHOW_SEARCH:
                            setSearchFocusedInternal(true);
                            break;
                        case LEFT_ACTION_MODE_SHOW_HOME:
                            if (mOnHomeActionClickListener != null) {
                                mOnHomeActionClickListener.onHomeClicked();
                            }
                            break;
                        case LEFT_ACTION_MODE_NO_LEFT_ACTION:
                            //do nothing
                            break;
                    }
                }

            }
        });

        refreshLeftIcon();
    }

    //ensures that the end margin of the search input is according to Material specs
    private void handleOnVisibleMenuItemsWidthChanged(int menuItemsWidth) {
        if (menuItemsWidth == 0) {
            mClearButton.setTranslationX(-Util.dpToPx(4));
            int paddingRight = Util.dpToPx(4);
            if (mIsFocused) {
                paddingRight += Util.dpToPx(CLEAR_BTN_WIDTH_DP);
            } else {
                paddingRight += Util.dpToPx(14);
            }
            mSearchInput.setPadding(0, 0, paddingRight, 0);
        } else {
            mClearButton.setTranslationX(-menuItemsWidth);
            int paddingRight = menuItemsWidth;
            if (mIsFocused) {
                paddingRight += Util.dpToPx(CLEAR_BTN_WIDTH_DP);
            }
            mSearchInput.setPadding(0, 0, paddingRight, 0);
        }
    }

    /**
     * Sets the menu button's color.
     *
     * @param color the color to be applied to the
     *              left menu button.
     */
    public void setLeftActionIconColor(int color) {
        mLeftActionIconColor = color;
        mMenuBtnDrawable.setColor(color);
        DrawableCompat.setTint(mIconBackArrow, color);
        DrawableCompat.setTint(mIconSearch, color);
    }

    /**
     * If set, the left menu won't open or close and the client is assumed to handle its
     * clicks.
     *
     * @param onMenuClickListener
     */
    public void setOnMenuClickListener(OnLeftMenuClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    /**
     * Sets the clear button's color.
     *
     * @param color the color to be applied to the
     *              clear button.
     */
    public void setClearBtnColor(int color) {
        mClearBtnColor = color;
        DrawableCompat.setTint(mIconClear, mClearBtnColor);
    }

    /**
     * Sets the action menu icons' color.
     *
     * @param color the color to be applied to the
     *              action menu items.
     */
    public void setMenuItemIconColor(int color) {
        this.mActionMenuItemColor = color;
        if (mMenuView != null) {
            mMenuView.setActionIconColor(this.mActionMenuItemColor);
        }
    }

    /**
     * Provides clients access to the menu items
     *
     * @return
     */
    public List<MenuItemImpl> getCurrentMenuItems() {
        return mMenuView.getCurrentMenuItems();
    }

    /**
     * Sets the action menu overflow icon's color.
     *
     * @param color the color to be applied to the
     *              overflow icon.
     */
    public void setActionMenuOverflowColor(int color) {
        this.mOverflowIconColor = color;
        if (mMenuView != null) {
            mMenuView.setOverflowColor(this.mOverflowIconColor);
        }
    }

    /**
     * Sets the text color of the search text.
     *
     * @param color
     */
    public void setQueryTextColor(int color) {
        mSearchInputTextColor = color;
        if (mSearchInput != null) {
            mSearchInput.setTextColor(mSearchInputTextColor);
        }
    }

    /**
     * Set the text size of the text in the search box.
     *
     * @param sizePx
     */
    public void setQueryTextSize(int sizePx) {
        mQueryTextSize = sizePx;
        mSearchInput.setTextSize(mQueryTextSize);
    }

    /**
     * Sets the text color of the search
     * hint.
     *
     * @param color the color to be applied to the search hint.
     */
    public void setHintTextColor(int color) {
        mSearchInputHintColor = color;
        if (mSearchInput != null) {
            mSearchInput.setHintTextColor(color);
        }
    }

    /**
     * Set the mode for the left action button.
     *
     * @param mode
     */
    public void setLeftActionMode(@LeftActionMode int mode) {
        mLeftActionMode = mode;
        refreshLeftIcon();
    }

    private void refreshLeftIcon() {
        int leftActionWidthAndMarginLeft = Util.dpToPx(LEFT_MENU_WIDTH_AND_MARGIN_START_DP);
        int queryTranslationX = 0;

        mLeftAction.setVisibility(VISIBLE);
        switch (mLeftActionMode) {
            case LEFT_ACTION_MODE_SHOW_HAMBURGER:
                mLeftAction.setImageDrawable(mMenuBtnDrawable);
                mMenuBtnDrawable.setProgress(MENU_BUTTON_PROGRESS_HAMBURGER);
                break;
            case LEFT_ACTION_MODE_SHOW_SEARCH:
                mLeftAction.setImageDrawable(mIconSearch);
                break;
            case LEFT_ACTION_MODE_SHOW_HOME:
                mLeftAction.setImageDrawable(mMenuBtnDrawable);
                mMenuBtnDrawable.setProgress(MENU_BUTTON_PROGRESS_ARROW);
                break;
            case LEFT_ACTION_MODE_NO_LEFT_ACTION:
                mLeftAction.setVisibility(View.INVISIBLE);
                queryTranslationX = -leftActionWidthAndMarginLeft;
                break;
        }
        mSearchInputParent.setTranslationX(queryTranslationX);
    }

    private void toggleLeftMenu() {
        if (mMenuOpen) {
            closeMenu(true);
        } else {
            openMenu(true);
        }
    }

    /**
     * <p/>
     * Enables clients to directly manipulate
     * the menu icon's progress.
     * <p/>
     * Useful for custom animation/behaviors.
     *
     * @param progress the desired progress of the menu
     *                 icon's rotation: 0.0 == hamburger
     *                 shape, 1.0 == back arrow shape
     */
    public void setMenuIconProgress(float progress) {
        mMenuBtnDrawable.setProgress(progress);
        if (progress == 0) {
            closeMenu(false);
        } else if (progress == 1.0) {
            openMenu(false);
        }
    }

    /**
     * Mimics a menu click that opens the menu. Useful for navigation
     * drawers when they open as a result of dragging.
     */
    public void openMenu(boolean withAnim) {
        mMenuOpen = true;
        openMenuDrawable(mMenuBtnDrawable, withAnim);
        if (mOnMenuClickListener != null) {
            mOnMenuClickListener.onMenuOpened();
        }
    }

    /**
     * Mimics a menu click that closes. Useful when fo navigation
     * drawers when they close as a result of selecting and item.
     *
     * @param withAnim true, will close the menu button with
     *                 the  Material animation
     */
    public void closeMenu(boolean withAnim) {
        mMenuOpen = false;
        closeMenuDrawable(mMenuBtnDrawable, withAnim);
        if (mOnMenuClickListener != null) {
            mOnMenuClickListener.onMenuClosed();
        }
    }

    /**
     * Set the hamburger menu to open or closed without
     * animating hamburger to arrow and without calling listeners.
     *
     * @param isOpen
     */
    public void setLeftMenuOpen(boolean isOpen) {
        mMenuOpen = isOpen;
        mMenuBtnDrawable.setProgress(isOpen ? 1.0f : 0.0f);
    }

    /**
     * Shows a circular progress on top of the
     * menu action button.
     * <p/>
     * Call hidProgress()
     * to change back to normal and make the menu
     * action visible.
     */
    public void showProgress() {
        mLeftAction.setVisibility(View.GONE);
        mSearchProgress.setAlpha(0.0f);
        mSearchProgress.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(mSearchProgress, "alpha", 0.0f, 1.0f).start();
    }

    /**
     * Hides the progress bar after
     * a prior call to showProgress()
     */
    public void hideProgress() {
        mSearchProgress.setVisibility(View.GONE);
        mLeftAction.setAlpha(0.0f);
        mLeftAction.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(mLeftAction, "alpha", 0.0f, 1.0f).start();
    }

    /**
     * Inflates the menu items from
     * an xml resource.
     *
     * @param menuId a menu xml resource reference
     */
    public void inflateOverflowMenu(int menuId) {
        mMenuId = menuId;
        mMenuView.reset(menuId, actionMenuAvailWidth());
        if (mIsFocused) {
            mMenuView.hideIfRoomItems(false);
        }
    }

    private int actionMenuAvailWidth() {
        if (isInEditMode()) {
            return mQuerySection.getMeasuredWidth() / 2;
        }
        return mQuerySection.getWidth() / 2;
    }

    /**
     * Set a hint that will appear in the
     * search input. Default hint is R.string.abc_search_hint
     * which is "search..." (when device language is set to english)
     *
     * @param searchHint
     */
    public void setSearchHint(String searchHint) {
        mSearchHint = searchHint != null ? searchHint : getResources().getString(R.string.abc_search_hint);
        mSearchInput.setHint(mSearchHint);
    }

    /**
     * Sets whether the the button with the search icon
     * will appear in the soft-keyboard or not.
     *
     * @param show to show the search button in
     *             the soft-keyboard.
     */
    public void setShowSearchKey(boolean show) {
        mShowSearchKey = show;
        if (show) {
            mSearchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        } else {
            mSearchInput.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }
    }


    /**
     * Sets whether the search will lose focus when the softkeyboard
     * gets closed from a back press
     *
     * @param closeSearchOnKeyboardDismiss
     */
    public void setCloseSearchOnKeyboardDismiss(boolean closeSearchOnKeyboardDismiss) {
        this.mCloseSearchOnSofteKeyboardDismiss = closeSearchOnKeyboardDismiss;
    }

    /**
     * Sets whether a dim background will show when the search is focused
     *
     * @param dimEnabled True to show dim
     */
    public void setDimBackground(boolean dimEnabled) {
        this.mDimBackground = dimEnabled;
        refreshDimBackground();
    }

    private void refreshDimBackground() {
        if (this.mDimBackground && mIsFocused) {
            mBackgroundDrawable.setAlpha(BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED);
        } else {
            mBackgroundDrawable.setAlpha(BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED);
        }
    }

    /**
     * Wrapper implementation for EditText.setFocusable(boolean focusable)
     *
     * @param focusable true, to make search focus when
     *                  clicked.
     */
    public void setSearchFocusable(boolean focusable) {
        mSearchInput.setFocusable(focusable);
        mSearchInput.setFocusableInTouchMode(focusable);
    }

    /**
     * Sets the title for the search bar.
     * <p/>
     * Note that after the title is set, when
     * the search gains focus, the title will be replaced
     * by the search hint.
     *
     * @param title the title to be shown when search
     *              is not focused
     */
    public void setSearchBarTitle(CharSequence title) {
        this.mTitleText = title.toString();
        mIsTitleSet = true;
        mSearchInput.setText(title);
    }

    /**
     * Sets the search text.
     * <p/>
     * Note that this is the different from
     * {@link #setSearchBarTitle(CharSequence title) setSearchBarTitle} in
     * that it keeps the text when the search gains focus.
     *
     * @param text the text to be set for the search
     *             input.
     */
    public void setSearchText(CharSequence text) {
        mIsTitleSet = false;
        setQueryText(text);
    }

    /**
     * Returns the current query text.
     *
     * @return the current query
     */
    public String getQuery() {
        return mOldQuery;
    }

    public void clearQuery() {
        mSearchInput.setText("");
    }


    private void setQueryText(CharSequence text) {
        mSearchInput.setText(text);
        //move cursor to end of text
        mSearchInput.setSelection(mSearchInput.getText().length());
    }


    public void clearSearchFocus() {
        if (mDimBackground) {
            fadeOutBackground();
        }
        mClearButton.setVisibility(View.GONE);
        mSkipTextChangeEvent = true;
        mSearchInput.setText("");
        if (mFocusChangeListener != null) {
            mFocusChangeListener.onFocusCleared();
        }
    }

    public boolean isSearchBarFocused() {
        return mIsFocused;
    }

    private void setSearchFocusedInternal(final boolean focused) {
        this.mIsFocused = focused;

        if (focused) {
            if (mDimBackground) {
                fadeInBackground();
            }
            handleOnVisibleMenuItemsWidthChanged(0);//this must be called before  mMenuView.hideIfRoomItems(...)
            mMenuView.hideIfRoomItems(true);
            transitionInLeftSection(true);
//            Util.showSoftKeyboard(getContext(), mSearchInput);
            if (mMenuOpen) {
                closeMenu(false);
            }
            if (mIsTitleSet) {
                mSkipTextChangeEvent = true;
                mSearchInput.setText("");
            } else {
                mSearchInput.setSelection(mSearchInput.getText().length());
            }
            mSearchInput.setLongClickable(true);
            mClearButton.setVisibility((mSearchInput.getText().toString().length() == 0) ?
                    View.INVISIBLE : View.VISIBLE);
            if (mFocusChangeListener != null) {
                mFocusChangeListener.onFocus();
            }
        } else {
            mMainLayout.requestFocus();
            if (mDimBackground) {
                fadeOutBackground();
            }
            handleOnVisibleMenuItemsWidthChanged(0);//this must be called before  mMenuView.hideIfRoomItems(...)
            mMenuView.showIfRoomItems(true);
            transitionOutLeftSection(true);
            mClearButton.setVisibility(View.GONE);
            if (mHostActivity != null) {
                Util.closeSoftKeyboard(mHostActivity);
            }
            if (mIsTitleSet) {
                mSkipTextChangeEvent = true;
                mSearchInput.setText(mTitleText);
            }
            mSearchInput.setLongClickable(false);
            if (mFocusChangeListener != null) {
                mFocusChangeListener.onFocusCleared();
            }
            mOnBackSearchButtonListener.onBackClick();
        }
    }

    public interface onBackSearchButtonListener {
        void onBackClick();
    }

    private void changeIcon(ImageView imageView, Drawable newIcon, boolean withAnim) {
        imageView.setImageDrawable(newIcon);
        if (withAnim) {
            ObjectAnimator fadeInVoiceInputOrClear = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f);
            fadeInVoiceInputOrClear.start();
        } else {
            imageView.setAlpha(1.0f);
        }
    }

    private void transitionInLeftSection(boolean withAnim) {

        if (mSearchProgress.getVisibility() != View.VISIBLE) {
            mLeftAction.setVisibility(View.VISIBLE);
        } else {
            mLeftAction.setVisibility(View.INVISIBLE);
        }

        switch (mLeftActionMode) {
            case LEFT_ACTION_MODE_SHOW_HAMBURGER:
                openMenuDrawable(mMenuBtnDrawable, withAnim);
                if (!mMenuOpen) {
                    break;
                }
                break;
            case LEFT_ACTION_MODE_SHOW_SEARCH:
                mLeftAction.setImageDrawable(mIconBackArrow);
                if (withAnim) {
                    mLeftAction.setRotation(45);
                    mLeftAction.setAlpha(0.0f);
                    ObjectAnimator rotateAnim = ViewPropertyObjectAnimator.animate(mLeftAction).rotation(0).get();
                    ObjectAnimator fadeAnim = ViewPropertyObjectAnimator.animate(mLeftAction).alpha(1.0f).get();
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.setDuration(500);
                    animSet.playTogether(rotateAnim, fadeAnim);
                    animSet.start();
                }
                break;
            case LEFT_ACTION_MODE_SHOW_HOME:
                //do nothing
                break;
            case LEFT_ACTION_MODE_NO_LEFT_ACTION:
                mLeftAction.setImageDrawable(mIconBackArrow);

                if (withAnim) {
                    ObjectAnimator searchInputTransXAnim = ViewPropertyObjectAnimator
                            .animate(mSearchInputParent).translationX(0).get();

                    mLeftAction.setScaleX(0.5f);
                    mLeftAction.setScaleY(0.5f);
                    mLeftAction.setAlpha(0.0f);
                    mLeftAction.setTranslationX(Util.dpToPx(8));
                    ObjectAnimator transXArrowAnim = ViewPropertyObjectAnimator.animate(mLeftAction).translationX(1.0f).get();
                    ObjectAnimator scaleXArrowAnim = ViewPropertyObjectAnimator.animate(mLeftAction).scaleX(1.0f).get();
                    ObjectAnimator scaleYArrowAnim = ViewPropertyObjectAnimator.animate(mLeftAction).scaleY(1.0f).get();
                    ObjectAnimator fadeArrowAnim = ViewPropertyObjectAnimator.animate(mLeftAction).alpha(1.0f).get();
                    transXArrowAnim.setStartDelay(150);
                    scaleXArrowAnim.setStartDelay(150);
                    scaleYArrowAnim.setStartDelay(150);
                    fadeArrowAnim.setStartDelay(150);

                    AnimatorSet animSet = new AnimatorSet();
                    animSet.setDuration(500);
                    animSet.playTogether(searchInputTransXAnim, transXArrowAnim, scaleXArrowAnim, scaleYArrowAnim, fadeArrowAnim);
                    animSet.start();
                } else {
                    mSearchInputParent.setTranslationX(0);
                }
                break;
        }
    }

    private void transitionOutLeftSection(boolean withAnim) {

        switch (mLeftActionMode) {
            case LEFT_ACTION_MODE_SHOW_HAMBURGER:
                closeMenuDrawable(mMenuBtnDrawable, withAnim);
                break;
            case LEFT_ACTION_MODE_SHOW_SEARCH:
                changeIcon(mLeftAction, mIconSearch, withAnim);
                break;
            case LEFT_ACTION_MODE_SHOW_HOME:
                //do nothing
                break;
            case LEFT_ACTION_MODE_NO_LEFT_ACTION:
                mLeftAction.setImageDrawable(mIconBackArrow);

                if (withAnim) {
                    ObjectAnimator searchInputTransXAnim = ViewPropertyObjectAnimator.animate(mSearchInputParent)
                            .translationX(-Util.dpToPx(LEFT_MENU_WIDTH_AND_MARGIN_START_DP)).get();

                    ObjectAnimator scaleXArrowAnim = ViewPropertyObjectAnimator.animate(mLeftAction).scaleX(0.5f).get();
                    ObjectAnimator scaleYArrowAnim = ViewPropertyObjectAnimator.animate(mLeftAction).scaleY(0.5f).get();
                    ObjectAnimator fadeArrowAnim = ViewPropertyObjectAnimator.animate(mLeftAction).alpha(0.5f).get();
                    scaleXArrowAnim.setDuration(300);
                    scaleYArrowAnim.setDuration(300);
                    fadeArrowAnim.setDuration(300);
                    scaleXArrowAnim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            //restore normal state
                            mLeftAction.setScaleX(1.0f);
                            mLeftAction.setScaleY(1.0f);
                            mLeftAction.setAlpha(1.0f);
                            mLeftAction.setVisibility(View.INVISIBLE);
                        }
                    });

                    AnimatorSet animSet = new AnimatorSet();
                    animSet.setDuration(350);
                    animSet.playTogether(scaleXArrowAnim, scaleYArrowAnim, fadeArrowAnim, searchInputTransXAnim);
                    animSet.start();
                } else {
                    mLeftAction.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }


    /**
     * Sets the listener that will listen for query
     * changes as they are being typed.
     *
     * @param listener listener for query changes
     */
    public void setOnQueryChangeListener(OnQueryChangeListener listener) {
        this.mQueryListener = listener;
    }

    /**
     * Sets the listener that will be called when
     * an action that completes the current search
     * session has occurred and the search lost focus.
     * <p/>
     * <p>When called, a client would ideally grab the
     * search or suggestion query from the callback parameter or
     * from {@link #getQuery() getquery} and perform the necessary
     * query against its data source.</p>
     *
     * @param listener listener for query completion
     */
    public void setOnSearchListener(OnSearchListener listener) {
        this.mSearchListener = listener;
    }

    /**
     * Sets the listener that will be called when the focus
     * of the search has changed.
     *
     * @param listener listener for search focus changes
     */
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        this.mFocusChangeListener = listener;
    }

    /**
     * Sets the listener that will be called when the
     * left/start menu (or navigation menu) is clicked.
     * <p/>
     * <p>Note that this is different from the overflow menu
     * that has a separate listener.</p>
     *
     * @param listener
     */
    public void setOnLeftMenuClickListener(OnLeftMenuClickListener listener) {
        this.mOnMenuClickListener = listener;
    }

    /**
     * Sets the listener that will be called when the
     * left/start home action (back arrow) is clicked.
     *
     * @param listener
     */
    public void setOnHomeActionClickListener(OnHomeActionClickListener listener) {
        this.mOnHomeActionClickListener = listener;
    }

    /**
     * Sets the listener that will be called when
     * an item in the overflow menu is clicked.
     *
     * @param listener listener to listen to menu item clicks
     */
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.mActionMenuItemListener = listener;
        //todo reset menu view listener
    }

    /**
     * Sets the listener that will be called when the
     * clear search text action button (the x to the right
     * of the search text) is clicked.
     *
     * @param listener
     */
    public void setOnClearSearchActionListener(OnClearSearchActionListener listener) {
        this.mOnClearSearchActionListener = listener;
    }

    private void openMenuDrawable(final DrawerArrowDrawable drawerArrowDrawable, boolean withAnim) {
        if (withAnim) {
            ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    float value = (Float) animation.getAnimatedValue();
                    drawerArrowDrawable.setProgress(value);
                }
            });
            anim.setDuration(MENU_ICON_ANIM_DURATION);
            anim.start();
        } else {
            drawerArrowDrawable.setProgress(1.0f);
        }
    }

    private void closeMenuDrawable(final DrawerArrowDrawable drawerArrowDrawable, boolean withAnim) {
        if (withAnim) {
            ValueAnimator anim = ValueAnimator.ofFloat(1.0f, 0.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    float value = (Float) animation.getAnimatedValue();
                    drawerArrowDrawable.setProgress(value);
                }
            });
            anim.setDuration(MENU_ICON_ANIM_DURATION);
            anim.start();
        } else {
            drawerArrowDrawable.setProgress(0.0f);
        }
    }

    private void fadeOutBackground() {
        ValueAnimator anim = ValueAnimator.ofInt(BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED, BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (Integer) animation.getAnimatedValue();
                mBackgroundDrawable.setAlpha(value);
            }
        });
        anim.setDuration(BACKGROUND_FADE_ANIM_DURATION);
        anim.start();
    }

    private void fadeInBackground() {
        ValueAnimator anim = ValueAnimator.ofInt(BACKGROUND_DRAWABLE_ALPHA_SEARCH_NOT_FOCUSED, BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (Integer) animation.getAnimatedValue();
                mBackgroundDrawable.setAlpha(value);
            }
        });
        anim.setDuration(BACKGROUND_FADE_ANIM_DURATION);
        anim.start();
    }

    private boolean isRTL() {

        Configuration config = getResources().getConfiguration();
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.isFocused = mIsFocused;
        savedState.query = getQuery();
        savedState.searchHint = mSearchHint;
        savedState.dismissOnOutsideClick = mDismissOnOutsideTouch;
        savedState.showSearchKey = mShowSearchKey;
        savedState.isTitleSet = mIsTitleSet;
        savedState.backgroundColor = mBackgroundColor;
        savedState.queryTextColor = mSearchInputTextColor;
        savedState.searchHintTextColor = mSearchInputHintColor;
        savedState.actionOverflowMenuColor = mOverflowIconColor;
        savedState.menuItemIconColor = mActionMenuItemColor;
        savedState.leftIconColor = mLeftActionIconColor;
        savedState.clearBtnColor = mClearBtnColor;
        savedState.dividerColor = mDividerColor;
        savedState.menuId = mMenuId;
        savedState.leftActionMode = mLeftActionMode;
        savedState.queryTextSize = mQueryTextSize;
        savedState.dimBackground = mDimBackground;
        savedState.dismissOnSoftKeyboardDismiss = mDismissOnOutsideTouch;
        savedState.dismissFocusOnSuggestionItemClick = mDismissFocusOnItemSelection;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mIsFocused = savedState.isFocused;
        mIsTitleSet = savedState.isTitleSet;
        mMenuId = savedState.menuId;
        mOldQuery = savedState.query;
        setSearchText(mOldQuery);
        setShowSearchKey(savedState.showSearchKey);
        setSearchHint(savedState.searchHint);
        setBackgroundColor(savedState.backgroundColor);
        setQueryTextColor(savedState.queryTextColor);
        setQueryTextSize(savedState.queryTextSize);
        setHintTextColor(savedState.searchHintTextColor);
        setActionMenuOverflowColor(savedState.actionOverflowMenuColor);
        setMenuItemIconColor(savedState.menuItemIconColor);
        setLeftActionIconColor(savedState.leftIconColor);
        setClearBtnColor(savedState.clearBtnColor);
        setLeftActionMode(savedState.leftActionMode);
        setDimBackground(savedState.dimBackground);
        setCloseSearchOnKeyboardDismiss(savedState.dismissOnSoftKeyboardDismiss);

        if (mIsFocused) {

            mBackgroundDrawable.setAlpha(BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED);
            mSkipTextChangeEvent = true;
            mSkipQueryFocusChangeEvent = true;

            mClearButton.setVisibility((savedState.query.length() == 0) ? View.INVISIBLE : View.VISIBLE);
            mLeftAction.setVisibility(View.VISIBLE);

            Util.showSoftKeyboard(getContext(), mSearchInput);
        }
    }

    static class SavedState extends BaseSavedState {

        private boolean isFocused;
        private String query;
        private int queryTextSize;
        private int suggestionTextSize;
        private String searchHint;
        private boolean dismissOnOutsideClick;
        private boolean showMoveSuggestionUpBtn;
        private boolean showSearchKey;
        private boolean isTitleSet;
        private int backgroundColor;
        private int suggestionsTextColor;
        private int queryTextColor;
        private int searchHintTextColor;
        private int actionOverflowMenuColor;
        private int menuItemIconColor;
        private int leftIconColor;
        private int clearBtnColor;
        private int suggestionUpBtnColor;
        private int dividerColor;
        private int menuId;
        private int leftActionMode;
        private boolean dimBackground;
        private long suggestionsSectionAnimSuration;
        private boolean dismissOnSoftKeyboardDismiss;
        private boolean dismissFocusOnSuggestionItemClick;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isFocused = (in.readInt() != 0);
            query = in.readString();
            queryTextSize = in.readInt();
            suggestionTextSize = in.readInt();
            searchHint = in.readString();
            dismissOnOutsideClick = (in.readInt() != 0);
            showMoveSuggestionUpBtn = (in.readInt() != 0);
            showSearchKey = (in.readInt() != 0);
            isTitleSet = (in.readInt() != 0);
            backgroundColor = in.readInt();
            suggestionsTextColor = in.readInt();
            queryTextColor = in.readInt();
            searchHintTextColor = in.readInt();
            actionOverflowMenuColor = in.readInt();
            menuItemIconColor = in.readInt();
            leftIconColor = in.readInt();
            clearBtnColor = in.readInt();
            suggestionUpBtnColor = in.readInt();
            dividerColor = in.readInt();
            menuId = in.readInt();
            leftActionMode = in.readInt();
            dimBackground = (in.readInt() != 0);
            suggestionsSectionAnimSuration = in.readLong();
            dismissOnSoftKeyboardDismiss = (in.readInt() != 0);
            dismissFocusOnSuggestionItemClick = (in.readInt() != 0);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isFocused ? 1 : 0);
            out.writeString(query);
            out.writeInt(queryTextSize);
            out.writeInt(suggestionTextSize);
            out.writeString(searchHint);
            out.writeInt(dismissOnOutsideClick ? 1 : 0);
            out.writeInt(showMoveSuggestionUpBtn ? 1 : 0);
            out.writeInt(showSearchKey ? 1 : 0);
            out.writeInt(isTitleSet ? 1 : 0);
            out.writeInt(backgroundColor);
            out.writeInt(suggestionsTextColor);
            out.writeInt(queryTextColor);
            out.writeInt(searchHintTextColor);
            out.writeInt(actionOverflowMenuColor);
            out.writeInt(menuItemIconColor);
            out.writeInt(leftIconColor);
            out.writeInt(clearBtnColor);
            out.writeInt(suggestionUpBtnColor);
            out.writeInt(dividerColor);
            out.writeInt(menuId);
            out.writeInt(leftActionMode);
            out.writeInt(dimBackground ? 1 : 0);
            out.writeLong(suggestionsSectionAnimSuration);
            out.writeInt(dismissOnSoftKeyboardDismiss ? 1 : 0);
            out.writeInt(dismissFocusOnSuggestionItemClick ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerListener();

    public void attachNavigationDrawerToMenuButton(@NonNull DrawerLayout drawerLayout) {
        drawerLayout.addDrawerListener(mDrawerListener);
        setOnLeftMenuClickListener(new NavDrawerLeftMenuClickListener(drawerLayout));
    }

    public void detachNavigationDrawerFromMenuButton(@NonNull DrawerLayout drawerLayout) {
        drawerLayout.removeDrawerListener(mDrawerListener);
        setOnLeftMenuClickListener(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        //remove any ongoing animations to prevent leaks
        //todo investigate if correct
//        ViewCompat.animate(mSuggestionListContainer).cancel();
    }

    private class DrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            setMenuIconProgress(slideOffset);
        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }

    private class NavDrawerLeftMenuClickListener implements OnLeftMenuClickListener {

        DrawerLayout mDrawerLayout;

        public NavDrawerLeftMenuClickListener(DrawerLayout drawerLayout) {
            mDrawerLayout = drawerLayout;
        }

        @Override
        public void onMenuOpened() {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }

        @Override
        public void onMenuClosed() {
            //do nothing
        }
    }
}

