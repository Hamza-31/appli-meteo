package com.example.meteo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class VMListProvider<P extends ListProvider<E>, E> extends AndroidViewModel {

    private final Application application;
    private static final String ERROR_WRONG_SERVER_RESPONSE = "ERROR_WRONG_SERVER_RESPONSE";
    private static final String ERROR_WRONG_JSON_RESPONSE = "ERROR_WRONG_JSON_RESPONSE";
    static final String STATE_LOADING_STARTS = "STATE_LOADING_STARTS";
    static final String STATE_NO_INTERNET = "STATE_NO_INTERNET";
    static final String STATE_LOADING_ENDS = "STATE_LOADING_ENDS";
    static final String STATE_DONE = "STATE_DONE";
    private final MutableLiveData<P> mldProvider = new MutableLiveData<>();
    private MutableLiveData<ArrayList<E>> mldList;
    private MutableLiveData<String> mldState = new MutableLiveData<>();

    public VMListProvider(@NonNull Application application) {
        super(application);
        // Récupérer la reférence de l'application.
        this.application = application;
    }

    public MutableLiveData<String> getMldState() {
        return mldState;
    }
    public void setStateDone() {
        mldState.setValue(STATE_DONE);
    }


    public MutableLiveData<ArrayList<E>> getmldList(boolean forceReload){
        // Si pas encore ou plus d'ArrayList, instancier et requêter le serveur du provider.
        if(mldList == null){
            mldList = new MutableLiveData<>();
            loadData();
        } else if (forceReload) {
            loadData();
        }
        // Retourner la liveData.
        return mldList;
    }

    public P getProvider() {
        return mldProvider.getValue();
    }

    public void setProvider(P provider){
        mldProvider.setValue(provider);
    }

    private void loadData(){
        // Si connexion intenet active, requêter le serveur provider.
        if(Util.isConnected(application))
            new AsyncTaskProvider().execute(mldProvider.getValue());
        else
            mldState.setValue(STATE_NO_INTERNET);
    }
    /**
     * L' API level 30 a marqué comme déprécié AsyncTask (Java) mais pas CoroutineScope (Kotlin)
     * alors que cette dernière est une stricte copie de la première. C'est une démarche marketing pour
     * pouvoir promouvoir Kotlin en poussant les devellopeur Java à utiliser Java.util.concurrent nettement moins abordable.
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskProvider extends AsyncTask<P, Void, ArrayList<E>> {

        @Override
        protected void onPreExecute() {
            // Signaler le début du changement.
            mldState.setValue(STATE_LOADING_STARTS);
        }

        @Override
        protected ArrayList<E> doInBackground(P... providers) {
            // Requêté le ListProvider.
            P provider = providers[0];
            // Préparer le inputStream.
            InputStream is;

            try {
                is = new URL(provider.getURL()).openStream();
            } catch (IOException e) {
                Log.d("ListProvider", ERROR_WRONG_SERVER_RESPONSE);
                return null;
            }
            // Lire la réponse ligne à ligne.
            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine())
                sb.append(scanner.nextLine());
            // Exploiter la réponse et récupérer une ArrayListe.
            ArrayList<E> list;
            try {
                list = provider.toList(sb.toString());
            } catch (Exception e) {
                Log.e("FragmentList", ERROR_WRONG_JSON_RESPONSE);
                return null;
            }
            // retourner l'ArrayList
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<E> list) {
            Log.d("list", list.toString());
            // Définir l'Arraylist liveData à ârtir de l'arrayList reçu.
            VMListProvider.this.mldList.setValue(list);
            mldState.setValue(STATE_LOADING_ENDS);
        }
    }
}