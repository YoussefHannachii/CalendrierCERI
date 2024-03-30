package com.example.calendrierceri.util;

public interface FiltreService {

    //Pour toutes les methodes si id edt personnel n'existe pas le cas sera traiter dans le code aprés

    public void onSalleFiltre(String searchDate,String filtreValue,int edtId,int personalEdtId);
    public void onTypeFiltre(String searchDate,String filtreValue,int edtId,int personalEdtId);
    public void onMatiereFiltre(String searchDate,String filtreValue,int edtId,int personalEdtId);
}
