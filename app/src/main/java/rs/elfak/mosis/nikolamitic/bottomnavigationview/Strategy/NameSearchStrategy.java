package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;

public class NameSearchStrategy implements SearchStrategy
{
    @Override
    public void search(String query, HashMap<Parking, Marker> mapParkingsMarkers) {
        for (Parking parking: mapParkingsMarkers.keySet())
        {
            Marker mMarker = null;

            mMarker = mapParkingsMarkers.get(parking);
            mMarker.setVisible(parking.getName().toLowerCase().startsWith(query.toLowerCase()));
            mMarker.showInfoWindow();
        }
    }
}
