/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.view;

import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.searchcomponents.core.view.ViewRequest;
import com.hp.autonomy.searchcomponents.core.view.ViewServerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractViewControllerTest<C extends ViewController<R, S, E>, R extends ViewRequest<S>, S extends Serializable, E extends Exception> {
    @Mock
    protected ControllerUtils controllerUtils;

    protected C viewController;
    protected ViewServerService<R, S, E> viewServerService;
    protected MockHttpServletResponse response;

    @Before
    public void setUp() {
        response = new MockHttpServletResponse();
        when(controllerUtils.buildErrorModelAndView(any(ErrorModelAndViewInfo.class))).thenReturn(mock(ModelAndView.class));
    }

    protected abstract S getSampleDatabase();

    @Test
    public void viewDocument() throws E, IOException {
        final String reference = "SomeReference";
        final S sampleDatabase = getSampleDatabase();
        viewController.viewDocument(reference, sampleDatabase, null, response);
        verify(viewServerService).viewDocument(any(), any(OutputStream.class));
    }
}
