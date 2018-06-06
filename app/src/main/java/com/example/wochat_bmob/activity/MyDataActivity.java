package com.example.wochat_bmob.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.bean.UserInfo;
import com.example.wochat_bmob.tools.ScreenSizeUtils;
import com.example.wochat_bmob.ui.ClearEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyDataActivity extends BaseActivity {

    @BindView(R.id.my_data_toolbar)
    Toolbar myDataToolbar;

    @BindView(R.id.toolbarTitle)
    TextView myDataToolbarTitle;

    @BindView(R.id.my_head)
    CircleImageView myHead;

    @BindView(R.id.name)
    TextView myName;

    @BindView(R.id.sex_spinner)
    Spinner sexSpinner;

    @BindView(R.id.age_edit)
    ClearEditText ageEdit;

    @BindView(R.id.local_edit)
    ClearEditText localEdit;

    @BindView(R.id.phone_edit)
    ClearEditText phoneEdit;

    @BindView(R.id.email_edit)
    ClearEditText emailEdit;

    @BindView(R.id.qianming_edit)
    ClearEditText qianmingEdit;

    @BindView(R.id.back)
    Button backBtn;

    @BindView(R.id.save)
    Button saveBtn;

    ProgressDialog mLoadingDialog;
    Dialog mBottomDialog;

    String[] sexs={"保密","男","女"};
    String sex;

    private User user;
    private UserInfo mUserInfo;

    private static final int TAKE_PHOTO=1;
    public static final int CHOOSE_PHOTO=2;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_data);

        /*获取info*/
        Bundle bundle= getIntent().getExtras();
        BmobIMUserInfo userInfo=(BmobIMUserInfo)bundle.getSerializable("info");
        log("userInfo---"+userInfo.getName()+" "+userInfo.getId()+" "+userInfo.getAvatar());

        user= BmobUser.getCurrentUser(User.class);
        mLoadingDialog=new ProgressDialog(this);
        mLoadingDialog.setMessage("正在加载数据");
        mLoadingDialog.show();

        /*初始化标题栏*/
        initToolbar();
        /*初始化Spinner*/
        initSpinner();
        /*初始化资料*/
        searchInfo();
        /*初始化mBottomDialog*/
        initmBottomDialog();
    }

    private void initToolbar(){
        setSupportActionBar(myDataToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myDataToolbarTitle.setText("个人资料");
        myDataToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initSpinner(){
        // 为下拉列表定义一个适配器,数据来自sex
        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sexs);
        sexSpinner.setAdapter(spinnerAdapter);
    }

    private void searchInfo(){
        BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
        query.addWhereEqualTo("name", user.getUsername());
        //返回1条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(1);
        query.findObjects(new FindListener<UserInfo>() {
            @Override
            public void done(List<UserInfo> object, BmobException e) {
                if(e==null){
                    mUserInfo=object.get(0);
                    initInfo();
                }else{
                    log("失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    private void initInfo(){
        log(mUserInfo.toString());
        myName.setText(mUserInfo.getName());
        /*设置sexSpinner的值*/
        for (int i=0;i<sexs.length;i++){
            if (sexs[i].equals(mUserInfo.getSex())){
                sexSpinner.setSelection(i,true);
                break;
            }
        }
        ageEdit.setText(mUserInfo.getAge()+"");
        localEdit.setText(mUserInfo.getLocal());
        phoneEdit.setText(mUserInfo.getPhone());
        emailEdit.setText(mUserInfo.getMail());
        qianmingEdit.setText(mUserInfo.getIntroduction());
        mLoadingDialog.dismiss();
    }

    private void initmBottomDialog(){
        mBottomDialog = new Dialog(this,R.style.Theme_AppCompat_Dialog);//,NormalDialogStyle
        View view = View.inflate(this, R.layout.dialog_bottom, null);
        mBottomDialog.setContentView(view);
        mBottomDialog.setCanceledOnTouchOutside(true);
//        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
        Window dialogWindow = mBottomDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;;// (int) (ScreenSizeUtils.getInstance(this).getScreenWidth())* 0.9f
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;//
        lp.gravity = Gravity.BOTTOM;
        dialogWindow.setAttributes(lp);

        Button camera=(Button)view.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        Button album=(Button)view.findViewById(R.id.album);
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //因为要获取SD卡的照片，所以需要获取WRITE_EXTERNAL_STORAGE权限
                if (ContextCompat.checkSelfPermission(
                        MyDataActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    //没有就申请获取
                    ActivityCompat.requestPermissions(
                            MyDataActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    Log.d("success:","openAlbum");
                    Intent intent=new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");//选择图片
                    //intent.setType(“audio/*”); //选择音频
                    //intent.setType(“video/*”); //选择视频
                    startActivityForResult(intent,CHOOSE_PHOTO);//打开相册
                    Log.d("success:","this is Album");
                }
            }
        });

        Button cancel=(Button)view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomDialog.dismiss();
            }
        });

    }

    @OnClick(R.id.back)
    protected void backOnClick(){
        finish();
    }

    @OnClick(R.id.save)
    protected void saveOnClick(){
        UserInfo userInfo=new UserInfo();
        userInfo.setSex(sexSpinner.getSelectedItem().toString());
        /*年龄匹配*/
        Pattern agePattern = Pattern.compile("^[0-9]{1,2}$");
        if (!agePattern.matcher(ageEdit.getText().toString()).matches()){
            toast("年龄需为0-99的数字");
            ageEdit.setText(mUserInfo.getAge()+"");
            return;
        }
        userInfo.setAge(Integer.parseInt(ageEdit.getText().toString()));

        userInfo.setLocal(localEdit.getText().toString());
        /*电话号码匹配*/
        Pattern phonePattern = Pattern.compile("(\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$");
        if (phoneEdit.getText().toString().equals(mUserInfo.getPhone())
                ||phonePattern.matcher(phoneEdit.getText()).matches() ){
            //
            userInfo.setPhone(phoneEdit.getText().toString());
        }else{
            toast("请填写正确的电话号码");
            phoneEdit.setText(mUserInfo.getPhone());
            return;
        }

        /*邮箱匹配*/
        Pattern mailPattern = Pattern.compile("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}$");
        if (emailEdit.getText().toString().equals(mUserInfo.getMail())
                ||mailPattern.matcher(emailEdit.getText()).matches()){
            userInfo.setMail(emailEdit.getText().toString());
        }else {
            toast("请填写正确的邮箱");
            emailEdit.setText(mUserInfo.getMail());
            return;
        }

        userInfo.setIntroduction(qianmingEdit.getText().toString());
        userInfo.update(mUserInfo.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    toast("个人资料更新成功");
                    finish();
                }else{
                    toast("更新失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });

    }

    @OnClick(R.id.my_head)
    protected void headOnClick(){
        mBottomDialog.show();
    }

    private void openCamera(){
        String imageName=mUserInfo.getName()+"_head.jpg";
        //通过Context.getExternalCacheDir()方法可以获取到当前应用缓存数据
        //SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据.
        File ouputImage=new File(getExternalCacheDir(),imageName);
        try{
            if (ouputImage.exists()){
                ouputImage.delete();
            }
            ouputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        //将file对象转换为URI对象
        if (Build.VERSION.SDK_INT>=24){//判断Android SDK版本号
            //7.0之后的系统，不允许直接使用本地真实路径的uri，会抛出错误
            //FileProvider是特殊的内容提取器（需要注册）
            // 可以将uri封装，共享给外部，第二个参数可以是任意唯一的字符串
            imageUri= FileProvider.getUriForFile(
                    MyDataActivity.this,"com.example.wochat_bmob.fileprovider",ouputImage);
        }else {//如果低于7.0,
            imageUri=Uri.fromFile(ouputImage);
        }
        //启动相机程序
        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
        //将拍摄的照片存储在SDcard的时候
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");//选择图片
        //intent.setType(“audio/*”); //选择音频
        //intent.setType(“video/*”); //选择视频
        startActivityForResult(intent,CHOOSE_PHOTO);//打开相册
        Log.d("success:","this is Album");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode==RESULT_OK){
                    //显示拍摄的照片,bitmap位图文件
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        myHead.setImageBitmap(bitmap);
                        mBottomDialog.dismiss();
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode==RESULT_OK){
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT>=19){//判断Android SDK版本号
                        //4.4及以上的系统，要对uri进行解析，因为选取相册中的照片不在返回真实的uri，而是一个封装过的uri
                        handleImageOnKitKat(data);
                    }else {//如果低于7.0,
                        handleImageBeforeKitKat(data);
                    }
                    mBottomDialog.dismiss();
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            //如果是DocumentUri类型的uri，则通过document id处理
            String docId=DocumentsContract.getDocumentId(uri);
            if (uri.getAuthority().equals("com.android.providers.media.documents")){
                String id=docId.split(":")[1];//解析出数字格式的id
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }
            else if (uri.getAuthority().equals("com.android.providers.downloads.documents")){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如果是content类型的uri，则使用普通方式处理
            imagePath=getImagePath(uri,null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的uri，直接获取图片路径即可
            imagePath=uri.getPath();
        }
        displayImage(imagePath);//根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);//根据图片路径显示图片
    }

    private String getImagePath(Uri uri,String selection){
        String path=null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if (imagePath!=null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            myHead.setImageBitmap(bitmap);
        }else {
            System.out.println(Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(MyDataActivity.this,"you denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}
