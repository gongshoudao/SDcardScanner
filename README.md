# SDcardScanner
目前来看，基本解决了部分乱七八糟的厂商SD卡路径乱放导致无法读取的问题。可以看这个文章
http://www.androidcycle.com/?p=81

## 用法
```java
ArrayList<StorageBean> storageDatas = StorageUtils.getStorageData(this);//获取所有存储介质
遍历storageDatas，得到StorageBean

//是否可移除，是则是外置存储，USB或SD卡
storageData.getRemovable();

//是否已挂载
storageData.getMounted().equalsIgnoreCase("mounted");

//获取此存储介质的真实路径
storageData.getPath();

```

机身存储信息
![机身存储](/assets/internal.png)

SD卡存储信息
![SD卡](/assets/sdcard.png)

## 原理
主要是利用反射获取每个存储介质的StorageVolume及其属性
```java
public static ArrayList<StorageBean> getStorageData(Context pContext) {
        final StorageManager storageManager = (StorageManager) pContext.getSystemService(Context.STORAGE_SERVICE);
        try {
            //得到StorageManager中的getVolumeList()方法的对象
            final Method getVolumeList = storageManager.getClass().getMethod("getVolumeList");
            //---------------------------------------------------------------------

            //得到StorageVolume类的对象
            final Class<?> storageValumeClazz = Class.forName("android.os.storage.StorageVolume");
            //---------------------------------------------------------------------
            //获得StorageVolume中的一些方法
            final Method getPath = storageValumeClazz.getMethod("getPath");
            Method isRemovable = storageValumeClazz.getMethod("isRemovable");

            Method mGetState = null;
            //getState 方法是在4.4_r1之后的版本加的，之前版本（含4.4_r1）没有
            // （http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.4_r1/android/os/Environment.java/）
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                try {
                    mGetState = storageValumeClazz.getMethod("getState");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            //---------------------------------------------------------------------

            //调用getVolumeList方法，参数为：“谁”中调用这个方法
            final Object invokeVolumeList = getVolumeList.invoke(storageManager);
            //---------------------------------------------------------------------
            final int length = Array.getLength(invokeVolumeList);
            ArrayList<StorageBean> list = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                final Object storageValume = Array.get(invokeVolumeList, i);//得到StorageVolume对象
                final String path = (String) getPath.invoke(storageValume);
                final boolean removable = (Boolean) isRemovable.invoke(storageValume);
                String state = null;
                if (mGetState != null) {
                    state = (String) mGetState.invoke(storageValume);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        state = Environment.getStorageState(new File(path));
                    } else {
                        if (removable) {
                            state = EnvironmentCompat.getStorageState(new File(path));
                        } else {
                            //不能移除的存储介质，一直是mounted
                            state = Environment.MEDIA_MOUNTED;
                        }
                        final File externalStorageDirectory = Environment.getExternalStorageDirectory();
                        Log.e(TAG, "externalStorageDirectory==" + externalStorageDirectory);
                    }
                }
                long totalSize = 0;
                long availaleSize = 0;
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    totalSize = StorageUtils.getTotalSize(path);
                    availaleSize = StorageUtils.getAvailaleSize(path);
                }
                
                StorageBean storageBean = new StorageBean();
                storageBean.setAvailableSize(availaleSize);
                storageBean.setTotalSize(totalSize);
                storageBean.setMounted(state);
                storageBean.setPath(path);
                storageBean.setRemovable(removable);
                list.add(storageBean);
            }
            return list;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
```




