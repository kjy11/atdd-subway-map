package nextstep.subway.line.application;

import static java.util.stream.Collectors.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.exception.NotFoundLineException;

@Service
@Transactional
public class LineService {
    private final LineRepository lineRepository;

    public LineService(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    public LineResponse saveLine(LineRequest request) {
        Line persistLine = lineRepository.save(request.toLine());
        return LineResponse.of(persistLine);
    }

    @Transactional(readOnly = true)
	public List<LineResponse> findLines() {
		return lineRepository.findAll().stream()
			.map(LineResponse::of)
			.collect(toList());
	}

	@Transactional(readOnly = true)
	public LineResponse findLine(Long lineId) {
		return lineRepository.findById(lineId)
			.map(LineResponse::of)
			.orElseThrow(() -> new NotFoundLineException(lineId));
	}

	public void updateLine(Long lineId, LineRequest lineRequest) {
		final Line findLine = lineRepository.findById(lineId)
			.orElseThrow(() -> new NotFoundLineException(lineId));

		findLine.update(lineRequest.toLine());
	}

	public void deleteLine(Long lineId) {
		lineRepository.delete(
			lineRepository.findById(lineId)
				.orElseThrow(() -> new NotFoundLineException(lineId))
		);
	}
}
