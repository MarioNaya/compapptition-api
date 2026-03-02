package com.compapption.api.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Clase base para tests unitarios (sin Spring context).
 * Activa MockitoExtension para @Mock / @InjectMocks.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {
}
