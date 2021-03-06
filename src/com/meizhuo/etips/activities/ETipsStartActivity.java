package com.meizhuo.etips.activities;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.meizhuo.etips.app.ClientConfig;
import com.meizhuo.etips.app.ImgSwitchInfo;
import com.meizhuo.etips.app.Preferences;
import com.meizhuo.etips.common.AndroidUtils;
import com.meizhuo.etips.common.CalendarManager;
import com.meizhuo.etips.common.ETipsContants;
import com.meizhuo.etips.common.SP;
import com.meizhuo.etips.model.ImgInfo;
import com.meizhuo.etips.service.ETipsCoreService;

/**
 * Change Log: ETips 2.0 1.移除每日一句saying, Has_Saying_load 将会在再后一个版本移除 2.根据版本来识别
 * since 11.28 <br>
 * 12.13 changelog<br>
 * 除去Application中的courseList ,专用到保存在SharedPreference<br>
 * since 2.1
 * 
 * @author Jayin Ton
 * 
 */
public class ETipsStartActivity extends BaseUIActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 必须在在setContentView()前设置
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		final View view = View.inflate(this, R.layout.acty_etips_start, null);
		initBackgroud(view);
		setContentView(view);
		setEnableSwipBack(false);
		initData();
		initLayout();
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ETipsContants.Start:
					toast("首次加载需要稍等片刻");
					break;
				case ETipsContants.Finish:
					checkImgDoadload();
					String versionName = null;
					try {
						versionName = AndroidUtils
								.getAppVersionName(getContext());
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					if (versionName != null
							&& !versionName.equals(Preferences
									.getAppVersion(getContext()))) {
						addUseage();
						// 跳转到 导航
						openActivity(ETipsGuidePage.class);
						Preferences.setAppVersion(getContext(), versionName);
					} else {
						openActivity(ETipsMainActivity.class);
					}
					ETipsStartActivity.this.finish();
					break;
				}

			}

		};
		new Thread(new Runnable() {

			@Override
			public void run() {
				loadPreference();
				Message msg = handler.obtainMessage();
				// initAlarm();
				msg.what = ETipsContants.Finish;
				handler.sendMessageDelayed(msg, 1500);
			}
		}).start();
	}

	protected void addUseage() {
		SP sp = new SP(ETipsContants.SP_NAME_Notes, this); 
		sp.add(java.lang.System.currentTimeMillis() + "", getString(R.string.useage));
		
	}

	protected void checkImgDoadload() {
		ImgInfo info = ImgSwitchInfo.getImgInfo(getContext());
		if (!info.isDownloaded() && info.getUrl() != null
				&& !info.getUrl().equals("")) {
			Intent service = new Intent(getContext(),
					ETipsCoreService.class);
			service.putExtra("url", info.getUrl());
			service.putExtra("displayTime",info.getDisplayTime());	
			service.putExtra("description", info.getDescription() );
			service.putExtra("continuance", info.getContinuance() );
			service.setAction(ETipsContants.Action_Service_Download_Pic);
			startService(service);
		}
		
	}

	// 判断是否显示下载下来的背景图
	@SuppressWarnings("deprecation")
	private void initBackgroud(View view) {
		if (ImgSwitchInfo.shouldDisplayImg(getContext())) {
			ImgInfo info = ImgSwitchInfo.getImgInfo(getContext());
			ImageView iv = (ImageView) view.findViewById(R.id.iv_bg);
			iv.setBackgroundDrawable(Drawable.createFromPath(ImgSwitchInfo
					.getImgSavePath(getContext()) + info.getName()));
		}
	}

	private void loadPreference() {
		ClientConfig.init(getContext());
		String versionName = null;
		try {
			versionName = AndroidUtils.getAppVersionName(getContext());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			versionName = "1";
		}
		if (versionName != null
				&& !versionName.equals(Preferences.getAppVersion(getContext()))) {
			// 第一次装新版本，就去社会资源默认的日期
			//Preferences.setCurrentWeek(getContext(), 1);
			Preferences.setCurrentWeek(getContext(), CalendarManager.getStartTermWeek());
		}
	}

	@Override
	protected void initLayout() {

	}

	@Override
	protected void initData() {

	}

}
