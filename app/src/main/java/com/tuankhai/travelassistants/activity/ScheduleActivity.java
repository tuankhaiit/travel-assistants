package com.tuankhai.travelassistants.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.tuankhai.travelassistants.R;
import com.tuankhai.travelassistants.activity.controller.ScheduleController;
import com.tuankhai.travelassistants.adapter.ScheduleAdapter;
import com.tuankhai.travelassistants.bottomsheet.MenuBottomSheet;
import com.tuankhai.travelassistants.bottomsheet.interfaces.OnItemMenuSheetBottomClickListener;
import com.tuankhai.travelassistants.library.slideractivity.Slider;
import com.tuankhai.travelassistants.library.slideractivity.model.SliderConfig;
import com.tuankhai.travelassistants.library.slideractivity.model.SliderPosition;
import com.tuankhai.travelassistants.library.stickyadapter.StickyListHeadersListView;
import com.tuankhai.travelassistants.utils.AppContansts;
import com.tuankhai.travelassistants.utils.Utils;
import com.tuankhai.travelassistants.webservice.DTO.AddScheduleDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ScheduleActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, AdapterView.OnItemLongClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, OnItemMenuSheetBottomClickListener {
    ScheduleController mController;

    private Toolbar toolbar;
    private SliderConfig mConfig;
    //FloatingActionButton fab;
    private SwipeRefreshLayout refreshLayout;

    //Rycycleview
    private StickyListHeadersListView lvSchedule;
    private ArrayList<AddScheduleDTO.Schedule> arrSchedule;
    private ScheduleAdapter mAdapter;

    //Dialog new
    boolean isNew = true;
    private Dialog dialogAddNew;
    private Button btnCancel, btnCreate;
    private EditText txtName;
    private TextView txtFromDate, txtToDate, txtTitle;
    private Calendar current = Calendar.getInstance();
    private Calendar fromDate = Calendar.getInstance();
    private Calendar toDate = Calendar.getInstance();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    //Dialog del
    private Dialog dialogDel;
    private Button btnCancelDel, btnDel;
    private TextView txtNameDel;

    private AddScheduleDTO.Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        addControls();
        addEvents();

//        RelativeLayout bottomSheetLayout = (RelativeLayout) findViewById(R.id.layout_bottom_sheet);
//        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(AppContansts.INTENT_DATA, AppContansts.REQUEST_LOGIN);
            startActivity(intent);
            return;
        }
        mController.getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_schedule:
                isNew = true;
                txtTitle.setText("Tạo lịch trình mới");
                txtName.setText("");
                txtFromDate.setText(simpleDateFormat.format(current.getTime()));
                txtToDate.setText(simpleDateFormat.format(current.getTime()));
                txtFromDate.setError(null);
                txtToDate.setError(null);
                dialogAddNew.show();
                txtName.requestFocus();
                return true;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setData(List<AddScheduleDTO.Schedule> list) {
        Collections.sort(list);
        arrSchedule.clear();
        arrSchedule.addAll(list);
        mAdapter = new ScheduleAdapter(this, arrSchedule);
        lvSchedule.setAdapter(mAdapter);
    }

    private void initSlider() {
        mConfig = new SliderConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorPrimary))
                .secondaryColor(getResources().getColor(R.color.colorPrimary))
                .position(SliderPosition.LEFT)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.2f)
                .edge(true)
                .edgeSize(0.2f)
                .build();
        Slider.attach(this, mConfig);
    }

    private void addEvents() {
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialogAddNew.show();
//            }
//        });
        refreshLayout.setOnRefreshListener(this);

        //Dialog new
        btnCancel.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        txtName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    txtName.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtName.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        btnCancelDel.setOnClickListener(this);
        btnDel.setOnClickListener(this);

        txtFromDate.setOnClickListener(this);
        txtToDate.setOnClickListener(this);
    }

    private void addControls() {
        mController = new ScheduleController(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        fab = (FloatingActionButton) findViewById(fab);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.title_activity_schedule));

        initSlider();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        arrSchedule = new ArrayList<>();
        lvSchedule = (StickyListHeadersListView) findViewById(R.id.lv_schedule);
        lvSchedule.setOnItemClickListener(this);
        lvSchedule.setOnItemLongClickListener(this);
        lvSchedule.setOnHeaderClickListener(this);
        lvSchedule.setOnStickyHeaderChangedListener(this);
        lvSchedule.setOnStickyHeaderOffsetChangedListener(this);
//        lvSchedule.addHeaderView(getLayoutInflater().inflate(R.layout.list_header, null));
//        lvSchedule.addFooterView(getLayoutInflater().inflate(R.layout.list_footer, null));
        lvSchedule.setEmptyView(findViewById(R.id.empty));
        lvSchedule.setDrawingListUnderStickyHeader(true);
        lvSchedule.setAreHeadersSticky(true);
        lvSchedule.setFastScrollEnabled(false);
        lvSchedule.setFastScrollAlwaysVisible(false);
        lvSchedule.setStickyHeaderTopOffset(-20);

        //Dialog new
        dialogAddNew = new Dialog(this, Utils.getAnimDialog(this));
        dialogAddNew.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAddNew.setContentView(R.layout.content_dialog_new_schedule);
        dialogAddNew.setCanceledOnTouchOutside(false);
        dialogAddNew.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btnCancel = dialogAddNew.findViewById(R.id.btn_cancel);
        btnCreate = dialogAddNew.findViewById(R.id.btn_create);
        txtTitle = dialogAddNew.findViewById(R.id.txt_title);
        txtName = dialogAddNew.findViewById(R.id.txt_name);
        txtFromDate = dialogAddNew.findViewById(R.id.txt_from_date);
        txtToDate = dialogAddNew.findViewById(R.id.txt_to_date);
        txtToDate.setText(simpleDateFormat.format(toDate.getTime()));
        txtFromDate.setText(simpleDateFormat.format(fromDate.getTime()));

        //Dialog del
        dialogDel = new Dialog(this, Utils.getAnimDialog(this));
        dialogDel.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDel.setContentView(R.layout.content_dialog_del_schedule);
        dialogDel.setCanceledOnTouchOutside(false);
        dialogDel.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btnCancelDel = dialogDel.findViewById(R.id.btn_cancel_del);
        btnDel = dialogDel.findViewById(R.id.btn_delete);
        txtNameDel = dialogDel.findViewById(R.id.txt_name_del);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        schedule = arrSchedule.get(position);
        new MenuBottomSheet(this).show(getSupportFragmentManager(), "MenuSheetBottomSchedule");
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, SchedulePlaceActivity.class);
        intent.putExtra(AppContansts.INTENT_DATA, arrSchedule.get(i).id);
        startActivity(intent);
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {

    }

    @Override
    public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset) {
        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            header.setAlpha(1 - (offset / (float) header.getMeasuredHeight()));
        }
    }

    @Override
    public void onStickyHeaderChanged(StickyListHeadersListView l, View header, int itemPosition, long headerId) {
        //Độ trong suốt của header
        header.setAlpha(1);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            }
        }, 500);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //Dialog new
            case R.id.btn_cancel:
                dialogAddNew.dismiss();
                break;
            case R.id.btn_create:
                if (txtName.getText().toString().trim().isEmpty()) {
                    Utils.showFaildToast(this, "Tên lịch trình không thể trống");
                    txtName.setError("Tên lịch trình không thể trống");
                } else {
                    txtName.setError(null);
                    if (isNew) {
                        mController.createNewSchedule(txtName.getText().toString(), fromDate, toDate);
                    } else {
                        mController.editSchedule(schedule, txtName.getText().toString(), fromDate, toDate);
                    }
                }
                break;
            case R.id.txt_from_date:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtName.getWindowToken(), 0);
                showDialogPickerFromDate();
                break;
            case R.id.txt_to_date:
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(txtName.getWindowToken(), 0);
                showDialogPickerToDate();
                break;
            //Dialog del
            case R.id.btn_cancel_del:
                dialogDel.dismiss();
                break;
            case R.id.btn_delete:
                mController.deleteSchedule(schedule);
                break;
        }
    }

    private void showDialogPickerToDate() {
        int mYear = toDate.get(Calendar.YEAR);
        int mMonth = toDate.get(Calendar.MONTH);
        int mDay = toDate.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog pickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                toDate.set(Calendar.YEAR, year);
                toDate.set(Calendar.MONTH, month);
                toDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                toDate.set(Calendar.HOUR_OF_DAY, 23);
                toDate.set(Calendar.MINUTE, 59);
                toDate.set(Calendar.SECOND, 59);
                txtToDate.setText(simpleDateFormat.format(toDate.getTime()));
                Log.e("status", year + "-" + month + " - " + dayOfMonth);
                Log.e("status", fromDate.getTimeInMillis() + "-" + toDate.getTimeInMillis());
                if (fromDate.getTimeInMillis() >= toDate.getTimeInMillis()) {
                    Log.e("status", fromDate.getTimeInMillis() + "-" + toDate.getTimeInMillis());
                    txtToDate.setError("Thời gian không đúng!");
                    Utils.showFaildToast(ScheduleActivity.this, "Ngày kết thúc phải sau ngày bắt đầu!");
                } else {
                    txtToDate.setError(null);
                }
            }
        }, mYear, mMonth, mDay);
        pickerDialog.setTitle("Đến ngày");
        pickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        int type = getIntent().getIntExtra(AppContansts.INTENT_DATA, 0);
        if (type == AppContansts.REQUEST_ADD_SCHEDULE) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void showDialogPickerFromDate() {
        int mYear = fromDate.get(Calendar.YEAR);
        int mMonth = fromDate.get(Calendar.MONTH);
        int mDay = fromDate.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog pickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                fromDate.set(Calendar.YEAR, year);
                fromDate.set(Calendar.MONTH, month);
                fromDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                fromDate.set(Calendar.HOUR_OF_DAY, 0);
                fromDate.set(Calendar.MINUTE, 0);
                fromDate.set(Calendar.SECOND, 0);
                txtFromDate.setText(simpleDateFormat.format(fromDate.getTime()));
                Log.e("status", year + "-" + month + " - " + dayOfMonth);
                Log.e("status", fromDate.getTimeInMillis() + "-" + current.getTimeInMillis());
                if (fromDate.getTimeInMillis() < current.getTimeInMillis()) {
                    txtFromDate.setError("Thời gian không đúng!");
                    Utils.showFaildToast(ScheduleActivity.this, "Không thể tạo lịch trình trong quá khứ!");
                } else {
                    txtFromDate.setError(null);
                    if (fromDate.getTimeInMillis() > toDate.getTimeInMillis()) {
                        toDate.set(Calendar.YEAR, year);
                        toDate.set(Calendar.MONTH, month);
                        toDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        toDate.set(Calendar.HOUR_OF_DAY, 23);
                        toDate.set(Calendar.MINUTE, 59);
                        toDate.set(Calendar.SECOND, 59);
                        txtToDate.setText(simpleDateFormat.format(toDate.getTime()));
                    }
                }
            }
        }, mYear, mMonth, mDay);
        pickerDialog.setTitle("Từ ngày");
        pickerDialog.show();
    }

    public void addFailure() {
        Utils.showFaildToast(this, "Thời gian trùng với lịch trình khác!");
    }

    public void addSuccess(AddScheduleDTO.Schedule result) {
        arrSchedule.add(result);
        Collections.sort(arrSchedule);
        mAdapter.notifyDataChange();
        Utils.showSuccessToast(this, "Thêm thành công lịch trình mới!");
        dialogAddNew.dismiss();
    }

    @Override
    public void onDeleteClick() {
        txtNameDel.setText("Xóa lịch trình " + schedule.name + "?");
        dialogDel.show();
    }

    @Override
    public void onEditClick() {
        if (schedule.getEnd().getTime() < current.getTimeInMillis()) {
            Utils.showFaildToast(this, "Không thể sửa lịch trình trong quá khứ!");
        } else if (Integer.valueOf(schedule.place) > 0) {
            Utils.showFaildToast(this, "Không thể sửa lịch trình đã có chi tiết!");
        } else {
            isNew = false;
            txtTitle.setText("Sửa lịch trình");
            dialogAddNew.show();
            txtName.setText(schedule.name);
            txtName.requestFocus();
            txtName.setSelection(txtName.getText().toString().length());
            txtFromDate.setText(simpleDateFormat.format(schedule.getStart()));
            txtToDate.setText(simpleDateFormat.format(schedule.getEnd()));
            fromDate.setTime(schedule.getStart());
            toDate.setTime(schedule.getEnd());
        }
    }

    public void deleteFailure() {
        Utils.showFaildToast(this, "Xóa không thành công!");
        dialogDel.dismiss();
    }

    public void deleteSuccess(AddScheduleDTO.Schedule schedule) {
        arrSchedule.remove(schedule);
        mAdapter.notifyDataChange();
        dialogDel.dismiss();
        Utils.showSuccessToast(this, "Xóa thành công " + schedule.name);
    }

    public void editScheduleFailure() {
        Utils.showFaildToast(this, "Thao tác không thành công!");
    }

    public void editScheduleSuccess(AddScheduleDTO scheduleDTO) {
        arrSchedule.remove(schedule);
        arrSchedule.add(scheduleDTO.result);
        Collections.sort(arrSchedule);
        mAdapter.notifyDataChange();
        Utils.showSuccessToast(this, "Cập nhật thành công " + scheduleDTO.result.name);
        dialogAddNew.dismiss();
    }
}
