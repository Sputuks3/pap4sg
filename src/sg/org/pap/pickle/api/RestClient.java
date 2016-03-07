package sg.org.pap.pickle.api;

import android.text.TextUtils;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import java.util.Date;
import java.util.List;
import retrofit.RequestInterceptor;
import retrofit.RequestInterceptor.RequestFacade;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.converter.GsonConverter;
import sg.org.pap.pickle.Config;
import sg.org.pap.pickle.api.data.ErrorResponse;
import sg.org.pap.pickle.api.data.HomeResponse;
import sg.org.pap.pickle.api.data.LoginRequest;
import sg.org.pap.pickle.api.data.MessageResponse;
import sg.org.pap.pickle.api.data.NewsResponse;
import sg.org.pap.pickle.api.data.PillarResponse;
import sg.org.pap.pickle.api.data.PillarsResponse;
import sg.org.pap.pickle.api.data.RepresentativeResponse;
import sg.org.pap.pickle.api.data.RepsListResponse;
import sg.org.pap.pickle.api.data.Response;
import sg.org.pap.pickle.api.data.TownResponse;
import sg.org.pap.pickle.api.data.UserUpdateResponse;
import sg.org.pap.pickle.api.data.YyfResponse;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.MessageContact;
import sg.org.pap.pickle.models.News;
import sg.org.pap.pickle.models.Pillar;
import sg.org.pap.pickle.models.Representative;
import sg.org.pap.pickle.models.RepresentativeDetails;
import sg.org.pap.pickle.models.Town;
import sg.org.pap.pickle.models.User;

public class RestClient {
    private static RestClient instance = null;
    public RestAdapter mAdapter = new Builder().setEndpoint(Config.BASE_URL).setRequestInterceptor(new RequestInterceptor() {
        public void intercept(RequestFacade request) {
            request.addHeader("X-API-KEY", "V6RGDEGQOSMoWf4YFzPmAbBGepWJaCCioSpnWXg2");
        }
    }).setConverter(new GsonConverter(new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).registerTypeAdapter(Date.class, new DateTypeAdapter()).create())).build();
    public PickleService mService = ((PickleService) this.mAdapter.create(PickleService.class));

    protected RestClient() {
    }

    public static RestClient getInstance() {
        if (instance == null) {
            instance = new RestClient();
        }
        return instance;
    }

    public User login(String id, String postalCode, String name, String token) {
        if (TextUtils.isEmpty(id)) {
            LoginRequest request = new LoginRequest();
            request.setPostalCode(postalCode);
            request.setName(name);
            return this.mService.login(name, postalCode, token, Message.TYPE_PDPA).getUser();
        }
        return this.mService.login(name, postalCode, id, token, Message.TYPE_PDPA).getUser();
    }

    public ErrorResponse loginFailed(String id, String postalCode, String name) {
        LoginRequest request = new LoginRequest();
        request.setPostalCode(postalCode);
        request.setName(name);
        return this.mService.loginFailed(name, postalCode);
    }

    public Message getMessages(String type) {
        MessageResponse response;
        if (TextUtils.isEmpty(type)) {
            response = this.mService.message();
        } else {
            response = this.mService.message(type);
        }
        if (response != null) {
            return response.getMessage();
        }
        return null;
    }

    public HomeResponse getNews(String postalCode) throws Exception {
        if (TextUtils.isEmpty(postalCode)) {
            throw new Exception("Postal Code Parameter is a required field");
        }
        HomeResponse response = this.mService.home(postalCode);
        return response != null ? response : null;
    }

    public News getNewsDetail(String id) throws Exception {
        if (TextUtils.isEmpty(id)) {
            throw new Exception("News ID is a required field");
        }
        NewsResponse response = this.mService.news(id);
        if (response != null) {
            return response.getNews();
        }
        return null;
    }

    public List<Representative> getRepresentatives(String postal) {
        RepsListResponse response;
        if (TextUtils.isEmpty(postal)) {
            response = this.mService.reps();
        } else {
            response = this.mService.reps(postal);
        }
        if (response != null) {
            return response.getRepresentatives();
        }
        return null;
    }

    public RepresentativeDetails getRepresentative(String id) throws Exception {
        if (TextUtils.isEmpty(id)) {
            throw new Exception("ID field is required");
        }
        RepresentativeResponse response = this.mService.rep(id);
        if (response != null) {
            return response.getRepresentative();
        }
        return null;
    }

    public PillarsResponse getPillars() {
        PillarsResponse response = this.mService.pillars();
        return response != null ? response : null;
    }

    public Pillar getPillar(String id) throws Exception {
        if (TextUtils.isEmpty(id)) {
            throw new Exception("ID is a required field");
        }
        PillarResponse response = this.mService.pillar(id);
        if (response != null) {
            return response.getPillar();
        }
        return null;
    }

    public Town getTown(String id) throws Exception {
        if (TextUtils.isEmpty(id)) {
            throw new Exception("ID is a required field");
        }
        TownResponse response = this.mService.town(id);
        if (response != null) {
            return response.getTown();
        }
        return null;
    }

    public User updateSubscription(String email, String subscribe, String name, String postalCode, String id) {
        UserUpdateResponse response;
        if (TextUtils.isEmpty(id)) {
            response = this.mService.subscription(subscribe, email, name, postalCode);
        } else {
            response = this.mService.subscription(id, subscribe, email, name, postalCode);
        }
        if (response != null) {
            return response.getUser();
        }
        return null;
    }

    public MessageContact getContacts() {
        return this.mService.contacts(Message.TYPE_HQ_CONTACT).getMessage();
    }

    public Response pushToken(String token) {
        return this.mService.deviceToken(token, Message.TYPE_PDPA);
    }

    public YyfResponse getYayf() {
        return this.mService.yayf();
    }
}
