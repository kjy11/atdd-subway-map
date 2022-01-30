package nextstep.subway.application.exception;

import static java.lang.String.format;

public class EntityNotFoundException extends BadRequestException {

	public EntityNotFoundException(long id) {
		super(format("Entity key '%s' is not exists", id));
	}
}