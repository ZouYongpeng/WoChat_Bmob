package com.example.wochat_bmob.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by 邹永鹏 on 2018/5/28.
 */

public class BaseFragment extends Fragment {

    private Toast toast;

    public void toast(final Object object){
        try{
            runOnMain(new Runnable() {
                @Override
                public void run() {
                    if (toast==null){
                        toast=Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
                    }else {
                        toast=Toast.makeText(getActivity(),object.toString(),Toast.LENGTH_SHORT);
                    }
                    toast.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void runOnMain(Runnable runnable){
        getActivity().runOnUiThread(runnable);
    }

    public void log(String msg){
        if(Config.DEBUG){
            Log.d(getActivity().getLocalClassName(),msg);
        }
    }

    public void startActivity(Class<? extends Activity> target, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), target);
        if (bundle != null)
            intent.putExtra(getActivity().getPackageName(), bundle);
        getActivity().startActivity(intent);
    }
}
