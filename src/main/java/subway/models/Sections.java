package subway.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

@Embeddable
public class Sections {

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public void add(Section section) {
        sections.add(section);
    }

    public List<Station> getStations() {
        return sections.stream()
            .map(Section::getStations)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
    }

    public void validateUpStation(Station upStation) {
        Station downwardEndPoint = sections.get(sections.size() - 1).getDownStation();
        if (!downwardEndPoint.getId().equals(upStation.getId())) {
            throw new IllegalArgumentException();
        }
    }

    public void validateDownStation(Station downStation) {
        getStations().forEach(station -> {
            if (downStation.getId().equals(station.getId())) {
                throw new IllegalArgumentException();
            }
        });
    }
}