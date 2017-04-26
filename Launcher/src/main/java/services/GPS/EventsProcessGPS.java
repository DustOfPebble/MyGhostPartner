package services.GPS;


import services.Base.SurveySnapshot;

public interface EventsProcessGPS {
    void processLocationChanged(SurveySnapshot Sample);
}
