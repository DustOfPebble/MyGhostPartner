package services.GPS;


import services.History.SurveySnapshot;

public interface EventsProcessGPS {
    void processLocationChanged(SurveySnapshot Sample);
}
