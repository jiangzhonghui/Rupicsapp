package com.yi4all.rupics.service;

import java.io.IOException;
import java.util.Date;

import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yi4all.rupics.db.UserModel;
import com.yi4all.rupics.json.MessageModel;
import com.yi4all.rupics.json.RemoteInvokeModel;
import com.yi4all.rupics.service.enumtype.RemoteRequestAction;

public class RemoteServiceImpl implements IRemoteService {

	private static final String LOG_TAG = "ServiceImpl";
	public static final String TOKEN_TAG = "$token$";
	
	public static int RETURN_CODE_SUCCESS = 0;
	public static String PRODUCT_NAME = "rupics";

	private static IRemoteService service;

	private String base_url;
	private String tokenId = "";
	private long tokenExpirationTime;
	private String sid = "";
	private ObjectMapper mapper = new ObjectMapper();

	private RemoteServiceImpl(String url) {
		this.base_url = url;
	}

	public static IRemoteService getInstance() {
		if (service == null) {

			 service = new RemoteServiceImpl("http://[server.ip]:9000/service");
		}
		return service;
	}

	public boolean isLogin() {
		return tokenId != null && tokenId.length() > 0 && tokenExpirationTime > new Date().getTime();
	}
	
	private void sureLogin() throws ServiceException{
		if(!isLogin()){
				loginDirect();
		}
	}

	@Override
	public UserModel loginDirect() throws ServiceException {
		try {
			ClientResource cr = new ClientResource(base_url
					+ "/quicklogin?sid=" + sid
					+ "&desc=ANDROID");
			// Workaround for GAE servers to prevent chunk encoding
			// cr.setRequestEntityBuffering(true);
			// cr.accept();
			// cr.head(MediaType.APPLICATION_JSON);

			TypeReference type = new TypeReference<MessageModel<UserModel>>() {
			};

			String jsonStr = cr.get().getText();
			MessageModel<UserModel> msg = mapper.readValue(jsonStr, type);
			if (!msg.isFlag()) {
				throw new ServiceException(
						ServiceException.ERROR_CODE_LOGIN_ERROR,
						msg.getMessage());
			} else {
				UserModel login = msg.getData();
				tokenId = login.getTokenid();
				tokenExpirationTime = new Date().getTime() + 25*60*1000;//set expiration time shorter than 30 minutes
				
				return login;
			}

		} catch (ResourceException e1) {
			e1.printStackTrace();
			throw new ServiceException(e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new ServiceException(e1);
		} catch (RuntimeException re) {
			re.printStackTrace();
			throw new ServiceException(re);
		}
	}

	@Override
	public UserModel login(String email, String password)
			throws ServiceException {
		sureLogin();
		try {
			ClientResource cr = new ClientResource(base_url
					+ "/login");
			// Workaround for GAE servers to prevent chunk encoding
			cr.setRequestEntityBuffering(true);
			// cr.accept(MediaType.APPLICATION_JSON);
			// cr.head(MediaType.APPLICATION_JSON);

			TypeReference type = new TypeReference<MessageModel<UserModel>>() {
			};
			
			String request = "{\"email\":\""
					+ email + "\",\"passwd\": \"" + password
					+ "\",\"desc\":\"ANDROID\",\"mobileid\": \"" + PRODUCT_NAME + "\"}";
			
			request = "{\"tokenid\":\"" + tokenId + "\",\"data\":" + request + "}";
			Representation rep = new JsonRepresentation(request);
			rep.setMediaType(MediaType.APPLICATION_JSON);

			String jsonStr = cr.post(rep).getText();

			MessageModel<UserModel> msg = mapper.readValue(jsonStr, type);
			if (!msg.isFlag()) {
				throw new ServiceException(
						ServiceException.ERROR_CODE_LOGIN_ERROR,
						msg.getMessage());
			} else {
				UserModel login = msg.getData();
				tokenId = login.getTokenid();
				tokenExpirationTime = new Date().getTime() + 25*60*1000;//set expiration time shorter than 30 minutes
				
				return login;
			}

		} catch (ResourceException e1) {
			e1.printStackTrace();
			throw new ServiceException(e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new ServiceException(e1);
		}
	}

	@Override
	public MessageModel<String> register(UserModel user)
			throws ServiceException {
		sureLogin();
		try {
			ClientResource cr = new ClientResource(base_url
					+ "/fit/common/user/register?email=" + user.getEmail()
					 + "&passwd="
					+ user.getPassword() + "&mobileid=" + user.getDeviceId());
			// Workaround for GAE servers to prevent chunk encoding
			cr.setRequestEntityBuffering(true);
			// cr.accept(MediaType.APPLICATION_JSON);
			// cr.head(MediaType.APPLICATION_JSON);
			
			String request = "{\"email\":\""
					+ user.getEmail() + "\",\"passwd\": \"" + user.getPassword()
					+ "\",\"mobileid\": \"" + user.getDeviceId() + "\"}";
			
			request = "{\"tokenid\":\"" + tokenId + "\",\"data\":" + request + "}";
			Representation rep = new JsonRepresentation(request);
			rep.setMediaType(MediaType.APPLICATION_JSON);

			String jsonStr = cr.post(rep).getText();

			return mapper.readValue(jsonStr, new TypeReference<MessageModel<String>>(){});

		} catch (ResourceException e1) {
			throw new ServiceException(e1);
		} catch (IOException e1) {
			throw new ServiceException(e1);
		}
	}

	@Override
	public void setBaseUrl(String url) {
		this.base_url = url;

	}

	@Override
	public String getBaseUrl() {
		return this.base_url;
	}

	@Override
	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getSid() {
		return sid;
	}

	@Override
	public <T> MessageModel<T> generalQuery(RemoteInvokeModel rim, TypeReference type)
			throws ServiceException {
		String url = rim.getUrl();
		try {
			sureLogin();
			
			url = url.replace(TOKEN_TAG, tokenId);
			
			ClientResource cr = new ClientResource(base_url + url);
			cr.setRequestEntityBuffering(true);
//			cr.accept(MediaType.APPLICATION_JSON);
//			 cr.head(MediaType.APPLICATION_JSON);
			
			String jsonStr;
			if(rim.getAction() == RemoteRequestAction.GET){
				jsonStr = cr.get().getText();
			}else{
				//assemble request data
				String request = "{\"tokenid\":\"" + tokenId + "\",\"data\":" + rim.getRequestBody() + "}";
				Representation rep = new JsonRepresentation(request);
//				rep.setMediaType(MediaType.APPLICATION_JSON);

				jsonStr = cr.post(rep).getText();
			}

			return mapper.readValue(jsonStr, type);

		} catch (ResourceException e1) {
			throw new ServiceException(e1);
		} catch (IOException e1) {
			throw new ServiceException(e1);
		}
		
	}

	@Override
	public <T> MessageModel<T> pushData2Server(RemoteInvokeModel rim)
			throws ServiceException {
		String url = rim.getUrl();
		MessageModel<T> result = null;
		try {
			sureLogin();
			
			url = url.replace(TOKEN_TAG, tokenId);
			
			ClientResource cr = new ClientResource(base_url + url);
			// Workaround for GAE servers to prevent chunk encoding
			cr.setRequestEntityBuffering(true);
			 cr.accept(MediaType.APPLICATION_JSON);
			 cr.head(MediaType.APPLICATION_JSON);
			String jsonStr;
			if(rim.getAction() == RemoteRequestAction.GET){
				jsonStr = cr.get().getText();
			}else{
				//assemble request data
				String request = "{\"tokenid\":\"" + tokenId + "\",\"data\":" + rim.getRequestBody() + "}";
				Representation rep = new JsonRepresentation(request);
				rep.setMediaType(MediaType.APPLICATION_JSON);

				jsonStr = cr.post(rep).getText();
			}

			
			result = mapper.readValue(jsonStr, new TypeReference<MessageModel<T>>(){});

		} catch (ResourceException e1) {
			throw new ServiceException(e1);
		} catch (IOException e1) {
			throw new ServiceException(e1);
		}
		
		return result;
	}

	@Override
	public MessageModel<String> forgetPassword(String phone, String email)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
