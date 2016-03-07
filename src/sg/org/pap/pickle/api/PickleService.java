package sg.org.pap.pickle.api;

import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import sg.org.pap.pickle.api.data.ContactsResponse;
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
import sg.org.pap.pickle.api.data.UserResponse;
import sg.org.pap.pickle.api.data.UserUpdateResponse;
import sg.org.pap.pickle.api.data.YyfResponse;

public interface PickleService {
    @GET("/message")
    ContactsResponse contacts(@Query("type") String str);

    @FormUrlEncoded
    @POST("/device_token")
    Response deviceToken(@Field("device_token") String str, @Field("device_type") String str2);

    @GET("/home")
    HomeResponse home(@Query("postal") String str);

    @FormUrlEncoded
    @POST("/login")
    UserResponse login(@Field("name") String str, @Field("postal_code") String str2, @Field("device_token") String str3, @Field("device_type") String str4);

    @FormUrlEncoded
    @POST("/login")
    UserResponse login(@Field("name") String str, @Field("postal_code") String str2, @Field("id") String str3, @Field("device_token") String str4, @Field("device_type") String str5);

    @POST("/login")
    UserResponse login(@Body LoginRequest loginRequest);

    @FormUrlEncoded
    @POST("/login")
    ErrorResponse loginFailed(@Field("name") String str, @Field("postal_code") String str2);

    @GET("/message")
    MessageResponse message();

    @GET("/message")
    MessageResponse message(@Query("type") String str);

    @GET("/news")
    NewsResponse news(@Query("id") String str);

    @FormUrlEncoded
    @POST("/pillar")
    PillarResponse pillar(@Field("id") String str);

    @GET("/pillars")
    PillarsResponse pillars();

    @FormUrlEncoded
    @POST("/rep")
    RepresentativeResponse rep(@Field("id") String str);

    @GET("/reps")
    RepsListResponse reps();

    @GET("/reps")
    RepsListResponse reps(@Query("postal") String str);

    @FormUrlEncoded
    @POST("/subscription")
    UserUpdateResponse subscription(@Field("subscribe") String str, @Field("email") String str2, @Field("name") String str3, @Field("postal_code") String str4);

    @FormUrlEncoded
    @POST("/subscription")
    UserUpdateResponse subscription(@Field("id") String str, @Field("subscribe") String str2, @Field("email") String str3, @Field("name") String str4, @Field("postal_code") String str5);

    @GET("/town")
    TownResponse town(@Query("id") String str);

    @POST("/yayf")
    YyfResponse yayf();
}
