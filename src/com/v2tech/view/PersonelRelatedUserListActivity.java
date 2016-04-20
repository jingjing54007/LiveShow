package com.v2tech.view;

import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.v2tech.frag.CardScanFragment;
import com.v2tech.frag.PersonelSearchBarFragment;
import com.v2tech.frag.UserListFragment;
import com.v2tech.presenter.PersonelRelatedUserListPresenter;
import com.v2tech.presenter.PersonelRelatedUserListPresenter.PersonelRelatedUserListPresenterUI;
import com.v2tech.v2liveshow.R;

public class PersonelRelatedUserListActivity extends FragmentActivity implements
		PersonelSearchBarFragment.PersonelSearchBarTextListener,
		UserListFragment.UserListFragmentConnector, PersonelRelatedUserListPresenterUI, View.OnClickListener {

	PersonelSearchBarFragment barFrag;
	UserListFragment listFrag;
	CardScanFragment csFrag;
	TextView title;
	View returnBtn;

	private PersonelRelatedUserListPresenter presenter;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.personel_related_user_list_activity);
		title = (TextView)findViewById(R.id.title_bar_center_tv);
		returnBtn = findViewById(R.id.title_bar_left_btn);
		
		int type = this.getIntent().getIntExtra("type", -1);
		presenter = new PersonelRelatedUserListPresenter(type, this);

		barFrag = (PersonelSearchBarFragment)this.getSupportFragmentManager().findFragmentById(R.id.search_fragment);
		listFrag = (UserListFragment)this.getSupportFragmentManager().findFragmentById(R.id.list_fragment);
		csFrag = (CardScanFragment)this.getSupportFragmentManager().findFragmentById(R.id.card_scan_fragment);
		

		barFrag.setListener(this);
		listFrag.setConnector(this);
		
		returnBtn.setOnClickListener(this);
		presenter.onUICreated();
	}

	@Override
	public int getCount() {
		return presenter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return presenter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return presenter.getItemId(position);
	}

	@Override
	public void update(int position, View convertView) {
		presenter.update(position, convertView);
	}

	@Override
	public View inflate() {
		View view = LayoutInflater.from(this).inflate(R.layout.personel_user_item, null);
		LocalBind lb = new LocalBind();
		view.setTag(lb);
		lb.avatar = (ImageView)view.findViewById(R.id.personel_item_avatar);
		lb.name = (TextView)view.findViewById(R.id.personel_item_name);
		lb.sign = (TextView)view.findViewById(R.id.personel_item_sg);
		lb.text = (TextView)view.findViewById(R.id.personel_item_cf_text);
		lb.btn = (ImageView)view.findViewById(R.id.personel_item_cf_btn);
		lb.btn.setOnClickListener(this);
		lb.gender = (ImageView)view.findViewById(R.id.personel_item_gender);
		view.setOnClickListener(l);
		return view;
	}

	@Override
	public void onTextChanged(String content) {
		presenter.onTextChanged(content);
	}
	
	
	
	
	
	private OnClickListener l = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent i = new Intent();
			i.setClass(PersonelRelatedUserListActivity.this, FansFollowActivity.class);
			PersonelRelatedUserListActivity.this.startActivity(i);
			
		}
		
	};
	
	public void refreshDataSet() {
		listFrag.notifyDatasetChanged();
	}
	
	public void updateItemAvatar(View parent, Bitmap bm) {
		
	}
	
	public void updateItemName(View parent, String name) {
		LocalBind lb = (LocalBind)parent.getTag();
		lb.name.setText(name);
	}
	public void updateItemText(View parent, String txt) {
		LocalBind lb = (LocalBind)parent.getTag();
		lb.text.setText(txt);
	}
	public void updateItemSn(View parent, String sn) {
		LocalBind lb = (LocalBind)parent.getTag();
		lb.sign.setText(sn);
	}
	public void updateItemBtnCancel(View parent) {
		LocalBind lb = (LocalBind)parent.getTag();
		lb.btn.setImageResource(R.drawable.personel_item_cancel_follow_icon);
	}
	public void updateItemBtnFollow(View parent) {
		LocalBind lb = (LocalBind)parent.getTag();
		lb.btn.setImageResource(R.drawable.personel_item_follow_icon);
	}
	
	public void updateItemBtnTag(View parent, Object tag) {
		LocalBind lb = (LocalBind)parent.getTag();
		lb.btn.setTag(tag);
	}
	
	public void  updateItemGender(View parent, boolean male) {
		LocalBind lb = (LocalBind)parent.getTag();
		if (male) {
			lb.gender.setImageResource(R.drawable.personel_item_gender_male);
		} else {
			lb.gender.setImageResource(R.drawable.personel_item_gender_female);
		}
	}
	
	
	
	public void showFansTitle() {
		removeCardFragment();
		title.setText(R.string.personel_user_list_title_fans);
	}
	
	public void showFriendsTitle() {
		removeCardFragment();
		title.setText(R.string.personel_user_list_title_friends);
	}
	public void showFollowTitle() {
		removeCardFragment();
		title.setText(R.string.personel_user_list_title_follow);
	}
	public void showFrinedInvitationTitle() {
		title.setText(R.string.personel_user_list_title_friend_invitation);
		
	}
	
	private void removeCardFragment() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.remove(csFrag);
		transaction.commit();
	}
	
	
	public void doFinish() {
		finish();
	}
	
	
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id) {
		case R.id.personel_item_cf_btn:
			presenter.onItemBtnClicked(v.getTag());
			break;
		case R.id.title_bar_left_btn:
			presenter.onReturnBtnClicked();
			break;
		}
	}










	class LocalBind {
		ImageView avatar;
		TextView name;
		TextView sign;
		TextView text;
		ImageView btn;
		ImageView gender;
	}
	
	
	

}