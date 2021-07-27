package org.techtown.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Group_add_dialog extends AppCompatActivity {
     Button submitBtn;
     Button cancelBtn;
     EditText tdl_input;
     RecyclerView recyclerView;
     GroupMemberAdapter adapter;
     EditText email_input;
     HashMap userTokens = new HashMap<>();
     String googleName;
     List<String> userUids = new ArrayList<>();

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_add_dialog);
        submitBtn = findViewById(R.id.submit_btn);
        cancelBtn = findViewById(R.id.cancel_btn);
        tdl_input = findViewById(R.id.tdl_input);

        Intent intent = getIntent();
        long uuid = intent.getLongExtra("uuid",0);
        googleName = intent.getStringExtra("nameGoogle");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        userTokens = (HashMap)intent.getSerializableExtra("hashIntent");

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tdl_name = tdl_input.getText().toString();
                if(!tdl_name.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("name", tdl_name);
                    setResult(RESULT_OK, intent);

//                //GroupUsers -> 그룹의 사용자들 관리하는 데이터베이스 테스트구현
//                myRef.child("GroupUsers").child(tdl_name).push().setValue(uuid);
                    finish();
                }
                else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Group_add_dialog.this).setMessage("그룹명을 입력해주세요");
                    dialog.create().show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerView2);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupMemberAdapter();
        Button plusBtn = findViewById(R.id.member_plus_btn);
        email_input = findViewById(R.id.email_input);

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String member_email = email_input.getText().toString();
                if(!userTokens.isEmpty() && userTokens.containsKey(member_email)) {
                    adapter.addItem(new GroupMember(member_email));
                    Log.d("group_add_Dialog", member_email);
                   Log.d("group_add_Dialog",userTokens.get(member_email).toString());
                    sendGcm(member_email);

                }
                else if (!userTokens.containsKey(member_email)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Group_add_dialog.this).setMessage("FRIENDO의 사용자가 아닙니다.");
                    dialog.create().show();
                }

                email_input.setText(null);

            }
        });
        recyclerView.setAdapter(adapter);
    }
    void sendGcm(String email) {
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = userTokens.get(email).toString();
        notificationModel.notification.title = "FRIENDO";
        notificationModel.notification.text = googleName + "님의 초대 수락하시겠습니까?";
        notificationModel.data.title="FRIENDO";
        notificationModel.notification.text = googleName +"님의 초대 수락하시겠습니까";
        Gson gson = new Gson();
        String json = gson.toJson(notificationModel);

        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AAAAlzEMvvg:APA91bEG25GVkmgSNafCqUkTA2Xv6lubz27ghch4a97yIfg0n7jAzAUcifNF4nhu5XWSCH96P4odfOis-BoliFKSU2nNgpUcrYV2qxWL5aDi4h0bNYG1axwJXIrtf_YiIc_fmwWcapah")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }
}