package com.frame.core.net.okhttp;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.frame.core.entity.JsonEntity;
import com.frame.core.crash.exception.InstanceFactoryException;
import com.frame.core.crash.exception.ResponseException;
import com.frame.core.entity.Mapper;
import com.frame.core.net.ICallback;
import com.frame.core.net.Retrofit.ErrorRep;
import com.frame.core.net.Retrofit.SuccessRep;
import com.frame.core.util.lifeful.Lifeful;
import com.frame.core.util.lifeful.LifefulRunnable;
import com.frame.core.util.BusProvider;
import com.frame.core.util.MapperFactory;
import com.frame.core.util.TLog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 远程服务器数据回调
 * Created by Administrator on 2015/10/16.
 */
public final class RepCallback implements Callback {

    private static final String TAG = RepCallback.class.getSimpleName();

    private static CustomHandler handler;
    public static final String ERROR_MSG_1 = "服务器繁忙";
    public static final String ERROR_MSG_2 = "数据异常";
    public static final String ERROR_MSG_3 = "网络连接中断";
    public static final int ERROR_CODE_0 = 0; //服务器返回的错误类型(网络状态200时返回服务器自定义的错误)
    public static final int ERROR_CODE_1 = 1; //网络异常返回的错误类型(网络状态不等于200时返回异常)
    public static final int ERROR_CODE_2 = 2; //其他异常返回的错误类型(网络状态200时数据结构解析异常)
    private final SparseArray<Object> obj;
    private OkCallbackListener httpData;
    private Mapper mapper;
    private Class clazz;
    private Class<? extends JsonEntity> templateClazz;

    private RepCallback() {
        obj = new SparseArray<>();
        httpData = new OkCallbackListener() {
            @Override
            public void onSuccess(Object data, String resBody) {

            }

            @Override
            public void onFailure(int errorCode, String msg) {

            }

            @Override
            public Lifeful lifeful() {
                return null;
            }
        };
    }

    private RepCallback(@NonNull OkCallbackListener httpData
            , @NonNull Class<? extends JsonEntity> templateClazz) {
        obj = new SparseArray<>();
        this.httpData = httpData;
        this.templateClazz = templateClazz;
    }

    private RepCallback(@NonNull OkCallbackListener httpData, @NonNull Class<? extends Mapper> mapperClazz, Class clazz
            , @NonNull Class<? extends JsonEntity> templateClazz) {
        obj = new SparseArray<>();
        this.httpData = httpData;
        try {
            this.mapper = MapperFactory.getInstance().achieveObj(mapperClazz);
            this.clazz = clazz != null ? clazz : this.mapper.getEntityClass();
        } catch (InstanceFactoryException e) {
            e.printStackTrace();
        }
        this.templateClazz = templateClazz;
    }

    private RepCallback(@NonNull OkCallbackListener httpData, @NonNull Mapper mapper
            , Class clazz, @NonNull Class<? extends JsonEntity> templateClazz) {
        obj = new SparseArray<>();
        this.httpData = httpData;
        this.mapper = mapper;
        this.clazz = clazz != null ? clazz : mapper.getEntityClass();
        this.templateClazz = templateClazz;
    }

    @SuppressWarnings("unchecked")
    private boolean analyzeJson(String body) throws Exception {
        TLog.i("body", body);
        JsonEntity data;
        if (clazz != null) {
            data = JsonEntity.fromJson(body, clazz, templateClazz);
        } else {
            data = JsonEntity.fromJson(body, templateClazz);
        }
        if (data != null) {
            if (data.isSuccess() && data instanceof JsonEntity.ArrayData) {
                obj.put(0, mapper.transformEntityCollection(((JsonEntity.ArrayData) data).getArrayData()));
            } else if (data.isSuccess() && data instanceof JsonEntity.Data) {
                obj.put(0, mapper.transformEntity(((JsonEntity.Data) data).getData()));
            } else if (data.isSuccess() && clazz != null) {
                throw new ResponseException(ERROR_CODE_2, "模板中未实现相应的接口");
            } else if (!data.isSuccess()) {
                obj.put(0, data.getMessage());
                obj.put(2, data.getCode());
            }
            return data.isSuccess();
        }
        throw new ResponseException(ERROR_CODE_2, "数据异常");
    }

    @SuppressWarnings("unchecked")
    private void sendResToMain(boolean isSuccess) {
        Runnable runnable = () -> {
            if (isSuccess) {
                if (httpData != null) {
                    httpData.onSuccess(obj.get(0), obj.get(1).toString());
                } else {
                    BusProvider.getInstance().post(new SuccessRep(obj));
                }
            } else {
                if (httpData != null) {
                    httpData.onFailure(obj.get(2) != null ? (int) obj.get(2) : ERROR_CODE_2, obj.get(0).toString());
                } else {
                    BusProvider.getInstance().post(new ErrorRep(obj.get(2) != null ? (int) obj.get(2) : ERROR_CODE_2, obj.get(0).toString()));
                }
            }
        };

        if (httpData != null && httpData.lifeful() != null) {
            getHandler().post(new LifefulRunnable(runnable, httpData.lifeful()));
        } else {
            getHandler().post(runnable);
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        obj.put(0, ERROR_MSG_1);
        obj.put(2, ERROR_CODE_1);
        sendResToMain(false);
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        TLog.i("time", Looper.myLooper() != Looper.getMainLooper());
        boolean isSuccess;
        if (response.isSuccessful()) {
            String entityBody = response.body().string();
            try {
                isSuccess = analyzeJson(entityBody);
                obj.put(1, entityBody);
            } catch (ResponseException e) {
                TLog.analytics("okHttp", e.getMessage());
                isSuccess = false;
                obj.put(0, e.getMessage());
                obj.put(2, e.getErrorCode());
            } catch (Exception e) {
                TLog.analytics("okHttp", e.getMessage());
                isSuccess = false;
                obj.put(0, ERROR_MSG_2);
                obj.put(2, ERROR_CODE_2);
                e.printStackTrace();
            }
        } else {
            isSuccess = false;
            obj.put(0, ERROR_MSG_1);
            obj.put(2, ERROR_CODE_1);
        }
        sendResToMain(isSuccess);
    }

    private static class CustomHandler extends Handler {
        private CustomHandler() {
            super(Looper.getMainLooper());
        }
    }

    private static Handler getHandler() {
        synchronized (RepCallback.class) {
            if (handler == null) {
                handler = new CustomHandler();
            }
            return handler;
        }
    }

    public interface OkCallbackListener<T> extends ICallback<T> {

        void onSuccess(T data, String resBody); //成功返回

        void onFailure(int errorCode, String msg); //反馈失败

        Lifeful lifeful();  //生命监控
    }

    public static final class Builder {

        private ICallback httpData;    //回调监听
        private Mapper mapper;                  //数据交接
        private Class clazz;                    //实体模型
        private Class<? extends Mapper> mapperClazz;                //映射类
        private Class<? extends JsonEntity> templateClazz;         //解析模板

        public Builder setListener(ICallback httpData) {
            this.httpData = httpData;
            return this;
        }

        public Builder setMapper(Mapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder setClass(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder setMapperClass(Class<? extends Mapper> mapperClazz) {
            this.mapperClazz = mapperClazz;
            return this;
        }

        public Builder setTempletClass(Class<? extends JsonEntity> templateClazz) {
            this.templateClazz = templateClazz;
            return this;
        }

        public RepCallback build() {
            if (httpData != null && httpData instanceof OkCallbackListener) {
                if (mapper != null && templateClazz != null) {
                    return new RepCallback((OkCallbackListener) httpData, mapper, clazz, templateClazz);
                } else if (mapperClazz != null && templateClazz != null) {
                    return new RepCallback((OkCallbackListener) httpData, mapperClazz, clazz, templateClazz);
                } else if (templateClazz != null) {
                    return new RepCallback((OkCallbackListener) httpData, templateClazz);
                } else {
                    return new RepCallback();
                }
            } else {
                return new RepCallback();
            }

        }
    }

}


