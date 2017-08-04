package com.zzzmode.appopsx.ui.main.group;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import com.zzzmode.appopsx.common.OpsResult;
import com.zzzmode.appopsx.ui.core.Helper;
import com.zzzmode.appopsx.ui.model.PermissionChildItem;
import com.zzzmode.appopsx.ui.model.PermissionGroup;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.observers.ResourceSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

/**
 * Created by zl on 2017/7/17.
 */

class PermGroupPresenter {

  private static final String TAG = "PermGroupPresenter";
  private IPermGroupView mView;

  private Context context;

  private ResourceSingleObserver<List<PermissionGroup>> subscriber;

  private List<PermissionGroup> mPermsGroup;

  PermGroupPresenter(IPermGroupView mView, Context context) {
    this.mView = mView;
    this.context = context;
  }


  void loadPerms() {
    boolean showSysApp = PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean("show_sysapp", false);
    
    subscriber = new ResourceSingleObserver<List<PermissionGroup>>() {
      @Override
      public void onSuccess(List<PermissionGroup> value) {
        Log.e(TAG, "subscriber onSuccess --> ");
        mView.showList(value);
      }

      @Override
      public void onError(Throwable e) {
        mView.showError(e);
      }

    };

    Helper.getPermissionGroup(context,showSysApp)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(subscriber);

  }


  void changeMode(final int groupPosition, final int childPosition,
      final PermissionChildItem info) {

    info.opEntryInfo.changeStatus();

    Helper.setMode(context, info.appInfo.packageName, info.opEntryInfo)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(new ResourceObserver<OpsResult>() {
          @Override
          public void onNext(OpsResult value) {

            mView.changeTitle(groupPosition, childPosition, info.opEntryInfo.isAllowed());
          }

          @Override
          public void onError(Throwable e) {
            try {
              info.opEntryInfo.changeStatus();
              mView.refreshItem(groupPosition, childPosition);
            } catch (Exception e2) {
              e2.printStackTrace();
            }
          }

          @Override
          public void onComplete() {

          }
        });
  }


  void destroy() {
    if(subscriber != null && !subscriber.isDisposed()) {
      subscriber.dispose();
    }
  }
}