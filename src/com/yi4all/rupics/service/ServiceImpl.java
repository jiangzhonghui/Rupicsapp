package com.yi4all.rupics.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yi4all.rupics.R;
import com.yi4all.rupics.db.CategoryModel;
import com.yi4all.rupics.db.ImageModel;
import com.yi4all.rupics.db.IssueModel;
import com.yi4all.rupics.db.UserModel;
import com.yi4all.rupics.json.MessageModel;
import com.yi4all.rupics.json.RemoteInvokeModel;
import com.yi4all.rupics.service.enumtype.RemoteInvokeStatus;
import com.yi4all.rupics.service.enumtype.RemoteRequestAction;
import com.yi4all.rupics.util.DateUtils;
import com.yi4all.rupics.util.Utils;

public class ServiceImpl {

	private static final String LOG_TAG = "ServiceImpl";

	private IDBService dbService;
	private IRemoteService remoteService;
	private UserModel currentUser;
	private Activity context;

	private ObjectMapper mapper;

	private static ServiceImpl instance;

	public static ServiceImpl getInstance(Activity context) {
		if (instance == null) {
			// initDBFile(context);

			instance = new ServiceImpl(context);
		}
		if (instance.remoteService == null) {
			instance.remoteService = RemoteServiceImpl.getInstance();
		}
		return instance;
	}

	private ServiceImpl(Activity context) {
		this.context = context;
		this.dbService = DBServiceImpl.getInstance(context);
		this.remoteService = RemoteServiceImpl.getInstance();

		this.mapper = new ObjectMapper();

		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String sid = manager.getDeviceId();

		this.remoteService.setSid(sid);
	}

	private IDBService getDbService() {
		if (dbService == null) {
			dbService = DBServiceImpl.getInstance(context);
		}
		return dbService;
	}

	private void setCurrentUser(UserModel currentUser) {
		this.currentUser = currentUser;
	}

	interface PushCallBack {
		public void execute(RemoteInvokeModel rim);

	}

	public interface QueryCallBack<T> {
		public void execute(T res);

	}
	
	public void addUserConsumedKbytes(int size){
		if(!sureLogin()) return;
		
		//1.subtract limit kb
		currentUser.setLimitKBytes(currentUser.getLimitKBytes() - size/1024);
		
		//2.submit to the server
		try {
			MessageModel result = remoteService.pushData2Server(createRIM(RemoteRequestAction.POST, "/service/user/limitkbytes/sub", ""));
			if (result.isFlag()) {
				try {
					String response = mapper.writeValueAsString(result.getData());
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

			} else {
				
			}
		} catch (ServiceException e) {
		}
	}
	
	public boolean sureLogin(){
		if(currentUser== null || !remoteService.isLogin()){
				MessageModel<UserModel> msg = loginDirect(remoteService.getSid());
				
				if(msg.isFlag()){
					currentUser = msg.getData();
				}else{
					Utils.toastMsg(context, R.string.login_error);
				}
		}
		return true;
	}
	
	public boolean sureLimitation(){
		if(!sureLogin()) return false;
		
		boolean flag = false;
		
		if(currentUser.getValidEndDate() != null){
			flag = currentUser.getValidEndDate().after(new Date()); 
		}else {
			flag = currentUser.getLimitKBytes() > 0; 
		}
		
		if(!flag){
			//TODO:popup buying tips
			Utils.toastMsg(context, R.string.limitation_error, DateUtils.defaultFormat(currentUser.getValidEndDate()), Utils.getKBSize(currentUser.getLimitKBytes()));
		}
		
		return true;
	}

	/********** 同步方法-远程 ****************/
	public UserModel getCurrentUser() {
		if (currentUser == null) {
			currentUser = getDbService().queryDefaultUser();
		}
		return currentUser;
	}

	public boolean createUser(UserModel user) {
		return getDbService().createUser(user);
	}

	public List<IssueModel> getIssueByCategoryRemote(CategoryModel category, int page) {
		if(!sureLogin()) return new ArrayList<IssueModel>();
		
		try {
			// 2.1.get return value
			TypeReference type = new TypeReference<MessageModel<List<IssueModel>>>() {
			};
			MessageModel<List<IssueModel>> result = remoteService.generalQuery(createRIM(RemoteRequestAction.GET,
					"/categories/" + category.getId() + "/issues/" + page,
					null),
					type);
			if (result != null && result.getData() != null) {

				getDbService().updateIssues(result.getData(), category);

				return result.getData();
			}

		} catch (ServiceException se) {
			Utils.toastMsg(context, R.string.netError, se.getMessage());
		}

		return new ArrayList<IssueModel>();
	}
	
	public List<ImageModel> getImageByIssueRemote(IssueModel issue) {
		if(!sureLogin()) return new ArrayList<ImageModel>();
		
		List<ImageModel> list = getDbService().getImageByIssue(issue);
		if(list != null && list.size() > 0){
			return list;
		}else{
		try {
			// 2.1.get return value
			TypeReference type = new TypeReference<MessageModel<List<ImageModel>>>() {
			};
			MessageModel<List<ImageModel>> result = remoteService.generalQuery(createRIM(RemoteRequestAction.GET,
					"/issues/" + issue.getId() + "/images",
					null),
					type);
			if (result != null && result.getData() != null) {

				getDbService().updateImages(result.getData(), issue);

				return result.getData();
			}

		} catch (ServiceException se) {
			Utils.toastMsg(context, R.string.netError, se.getMessage());
		}

		return new ArrayList<ImageModel>();
		}
	}

	public void getAllCategory(final QueryCallBack<List<CategoryModel>> query, boolean isRemote) {
		if(!sureLogin()) return ;
		
		List<CategoryModel> list = getDbService().getAllCategory();

		Runnable run = new Runnable() {

			@Override
			public void run() {
				try {
					// 2.1.get return value
					TypeReference type = new TypeReference<MessageModel<List<CategoryModel>>>() {
					};

					MessageModel<List<CategoryModel>> result = remoteService
							.generalQuery(createRIM(RemoteRequestAction.GET, "/categories", null), type);
					if (result != null && result.getData() != null) {
						query.execute(result.getData());

//						Utils.savePref(context, "lastUpdateCategoryTime", String.valueOf(new Date().getTime()));

						getDbService().updateCategories(result.getData());
					}

				} catch (ServiceException se) {

				}

			}
		};

		if (list.size() > 0) {
			query.execute(list);
			
			if(isRemote){
				new Thread(run).start();
			}

		} else {
			new Thread(run).start();
		}
	}

	public List<IssueModel> getIssueByCategory(CategoryModel category, int page) {
		if(!sureLogin()) return new ArrayList<IssueModel>();
		
		return getDbService().getIssueByCategory(category, page);
	}

	public List<ImageModel> getImageByIssue(IssueModel issue) {
		if(!sureLogin()) return new ArrayList<ImageModel>();
		
		return getDbService().getImageByIssue(issue);
	}

	public MessageModel<UserModel> loginDirect(String sid) {
		MessageModel<UserModel> msg = new MessageModel<UserModel>();
		try {
			UserModel user = remoteService.loginDirect();

			// query user from local db
			UserModel user2 = getDbService().queryUserBySid(sid);
			if (user2 == null) {
				// save user into db
				if (!getDbService().createUser(user)) {
					msg.setFlag(false);
					msg.setErrorCode(ServiceException.ERROR_CODE_DB_EXCEPTION);
					return msg;
				}
			} else {
				user2.setLimitKBytes(user.getLimitKBytes());
				user2.setValidEndDate(user.getValidEndDate());

				getDbService().updateUser(user2);
				user = user2;
			}

			setCurrentUser(user);
			msg.setData(user);
			msg.setFlag(true);
		} catch (ServiceException e) {
			Log.e(LOG_TAG, "loginDirect error:" + e.getMessage());
			msg.setErrorCode(e.getErrorCode());
			msg.setMessage(e.getMessage());
		}
		return msg;
	}

	public void pushData2Server(final RemoteInvokeModel rim, final PushCallBack pcb) {
		Runnable run = new Runnable() {

			@Override
			public void run() {
				try {
					MessageModel result = remoteService.pushData2Server(rim);
					if (result.isFlag()) {
						rim.setStatus(RemoteInvokeStatus.SUCCEED);
						try {
							String response = mapper.writeValueAsString(result.getData());
							rim.setResponseBody(response);
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}

					} else {
						rim.setStatus(RemoteInvokeStatus.FAILED);
					}
				} catch (ServiceException e) {
					rim.setStatus(RemoteInvokeStatus.FAILED);
				}

				// call back
				if (pcb != null) {
					pcb.execute(rim);
				}
			}
		};
		new Thread(run).start();
	}

	private RemoteInvokeModel createRIM(RemoteRequestAction action, String url, String request) {
		RemoteInvokeModel rim = new RemoteInvokeModel();
		// add params for RPC
		rim.setAction(action);
		rim.setRequestBody(request);
		rim.setStatus(RemoteInvokeStatus.ONGOING);
		rim.setUrl(url);

		return rim;
	}

	public void close() {
		if (dbService != null) {
			dbService.close();
			dbService = null;
		}
	}

	public void setURL(String url) {
		remoteService.setBaseUrl(url);
	}

	public String getURL() {
		return remoteService.getBaseUrl();
	}

	public void setContext(Activity context) {
		this.context = context;
	}

}
