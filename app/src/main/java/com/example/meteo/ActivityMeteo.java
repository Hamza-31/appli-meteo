package com.example.meteo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.meteo.databinding.ActivityMeteoBinding;
import com.google.android.material.snackbar.Snackbar;

public class ActivityMeteo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appel de la méthode parente.
        super.onCreate(savedInstanceState);
        // Récupérer la classe auto-générée de biding du layout.
        ActivityMeteoBinding binding = ActivityMeteoBinding.inflate(this.getLayoutInflater());
        // Afficher le layout.
        this.setContentView(binding.getRoot());
        // Supporter l'ActionBar via une ToolBar.
        this.setSupportActionBar(binding.toolbar);
        // Récupérer l'instance Singleton de VMListProvider.
        VMListProvider<OWM, OWM.Observation> vm = new ViewModelProvider(this).get(VMListProvider.class);
        // Définir le provider dans le ViewModel.
        vm.setProvider(new OWM());
        // Préparer la snackbar du chargement.
        Snackbar loadingBar = Snackbar.make(binding.container, R.string.info_loading, Snackbar.LENGTH_INDEFINITE);
        // La première fois, charger une instance de FragmentList dans le FrameLayout.
        // Observer la liste du provider.
        vm.getMldState().observe(this, state-> {
            switch (state){
                case VMListProvider.STATE_NO_INTERNET:
                    // Afficher le Snackbar du chargement.
                    loadingBar.make(binding.container, R.string.info_no_internet, Snackbar.LENGTH_LONG).show();
                    break;
                case VMListProvider.STATE_LOADING_STARTS:
                    // Afficher le Snackbar du chargement.
                    loadingBar.show();
                    break;
                case VMListProvider.STATE_LOADING_ENDS:
                    // Afficher le Snackbar du chargement.
                    loadingBar.dismiss();
                    break;
                case VMListProvider.STATE_DONE:
                    // Ne rien faire, présent pour éviter une boucle sans fin.
                    return;
            }
            vm.setStateDone();
        });
        if (savedInstanceState == null) {
            this
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new FragmentList())
                    .commit();
        }
    }
}