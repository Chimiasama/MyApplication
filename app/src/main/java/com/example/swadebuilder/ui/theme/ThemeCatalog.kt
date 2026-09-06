package com.example.swadebuilder.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class AppThemeData(
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val typography: Typography,
    @DrawableRes val backgroundDrawable: Int? = null,
    val cardBorderColor: Color? = null // PASSO 2: Borda para efeito Wireframe
)

// ─── Default Theme (Old School Paper) ────────────────────────────────────────
val DefaultThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = OldSchoolRed,
        onPrimary = Color.White,
        // Container opacos e propositais, não mais um overlay translúcido de
        // OldSchoolRed/Gold sobre o fundo — essa técnica antiga lia como um rosa
        // genérico e desbotado (a cor "legado" removida a pedido), sem relação
        // com a identidade vinho/dourado/pergaminho do tema. primaryContainer
        // puxa pro dourado/terracota (não pro vermelho-claro, que sempre lê rosa
        // quando clareado), secondaryContainer é o pergaminho dourado mais claro.
        primaryContainer = Color(0xFFE7CCB0),
        onPrimaryContainer = OldSchoolInk,
        secondary = OldSchoolGold,
        onSecondary = OldSchoolInk,
        secondaryContainer = Color(0xFFF1E2C4),
        onSecondaryContainer = OldSchoolInk,
        tertiary = OldSchoolInkSoft,
        onTertiary = Color.White,
        // tertiaryContainer nunca tinha sido definido em nenhum dos 8 temas —
        // caía sempre no rosa-claro padrão do Material3 (ex.: card de Vantagem/
        // Complicação/Poder "já selecionado"), sem nenhuma relação com a
        // identidade do tema. Tom de pergaminho envelhecido, puxando pro
        // castanho de OldSchoolInkSoft em vez de rosa.
        tertiaryContainer = Color(0xFFE3DCD2),
        onTertiaryContainer = OldSchoolInk,
        background = OldSchoolPaper,
        onBackground = OldSchoolInk,
        surface = OldSchoolSurface,
        onSurface = OldSchoolInk,
        surfaceVariant = OldSchoolSurfaceVariant,
        onSurfaceVariant = OldSchoolInkSoft,
        // Idem: sem isso, um card de "requisito pendente" (Vantagens/
        // Complicações/Progressos) caía no lavanda-padrão do Material3 em vez
        // de um cinza-pergaminho neutro — a mesma limpeza acima, só que pro
        // card que sinaliza "ainda não disponível" (não é bem um "erro").
        surfaceContainerHighest = Color(0xFFDCD3C6),
        outline = OldSchoolOutline,
        outlineVariant = OldSchoolOutline.copy(alpha = 0.6f),
        errorContainer = Color(0xFFF5D9D9),
        onErrorContainer = OldSchoolInk
    ),
    darkColors = darkColorScheme(
        primary = OldSchoolRed,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF4B3626),
        onPrimaryContainer = OldSchoolPaper,
        secondary = OldSchoolGold,
        onSecondary = OldSchoolDarkBg,
        secondaryContainer = Color(0xFF4F4126),
        onSecondaryContainer = OldSchoolPaper,
        tertiary = OldSchoolInkSoft,
        onTertiary = OldSchoolPaper,
        tertiaryContainer = Color(0xFF3A322A),
        onTertiaryContainer = OldSchoolPaper,
        background = OldSchoolDarkBg,
        onBackground = OldSchoolPaper,
        surface = OldSchoolDarkSurface,
        onSurface = OldSchoolPaper,
        surfaceVariant = OldSchoolDarkVariant,
        onSurfaceVariant = OldSchoolPaper.copy(alpha = 0.9f),
        surfaceContainerHighest = Color(0xFF35302A),
        outline = OldSchoolOutline.copy(alpha = 0.5f),
        outlineVariant = OldSchoolOutline.copy(alpha = 0.35f),
        errorContainer = Color(0xFF4A2220),
        onErrorContainer = OldSchoolPaper
    ),
    typography = DefaultTypography
)

// ─── Medieval Theme ──────────────────────────────────────────────────────────
val MedievalThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        // Opacos, não mais um overlay translúcido (mesma limpeza do tema Old
        // School): primaryContainer/secondaryContainer eram um blend de
        // MedGold/MedInk que lia como um tom genérico sem identidade própria, e
        // errorContainer era literalmente MedPaper (o próprio papel, sem tom de
        // alerta nenhum). Agora são tons de pergaminho/bronze opacos e um
        // vermelho-terra sóbrio pra erro.
        primaryContainer = Color(0xFFDED2C2),
        onPrimaryContainer = MedInk,
        secondary = MedInk,
        onSecondary = MedPaper,
        secondaryContainer = Color(0xFFEFE6D3),
        onSecondaryContainer = MedInk,
        tertiary = MedGold.copy(alpha = 0.8f),
        onTertiary = MedPaper,
        // tertiaryContainer nunca tinha sido definido — mesma limpeza do rosa-
        // padrão do Material3 aplicada aos outros containers deste tema.
        tertiaryContainer = Color(0xFFF0E4C8),
        onTertiaryContainer = MedInk,
        background = MedPaper,
        onBackground = MedInk,
        surface = MedPaper.copy(alpha = 0.98f),
        onSurface = MedInk,
        surfaceVariant = MedPaper.copy(alpha = 0.9f),
        onSurfaceVariant = MedInk.copy(alpha = 0.85f),
        surfaceContainerHighest = Color(0xFFE6DCC8),
        outline = MedGold.copy(alpha = 0.45f),
        outlineVariant = MedGold.copy(alpha = 0.25f),
        errorContainer = Color(0xFFF0D6D3),
        onErrorContainer = MedInk
    ),
    darkColors = darkColorScheme(
        primary = MedGold,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF55392C),
        onPrimaryContainer = MedPaper,
        secondary = MedPaper,
        onSecondary = MedInk,
        secondaryContainer = Color(0xFF6B4A38),
        onSecondaryContainer = MedPaper,
        tertiary = MedGold.copy(alpha = 0.9f),
        onTertiary = MedInk,
        tertiaryContainer = Color(0xFF5C4A2A),
        onTertiaryContainer = MedPaper,
        background = MedInk,
        onBackground = MedPaper,
        surface = Color(0xFF2B1E1A),
        onSurface = MedPaper,
        surfaceVariant = Color(0xFF3A2A24),
        onSurfaceVariant = MedPaper.copy(alpha = 0.9f),
        surfaceContainerHighest = Color(0xFF463327),
        outline = MedGold.copy(alpha = 0.4f),
        outlineVariant = MedGold.copy(alpha = 0.25f),
        errorContainer = Color(0xFF3A1E1E),
        onErrorContainer = MedPaper
    ),
    typography = MedievalTypography
)

// ─── Cyberpunk Theme (Matrix Style) ──────────────────────────────────────────
// PASSO 2: Fundo preto e Borda Wireframe
// HOTFIX: Texto branco forçado para legibilidade contra fundo preto
// HOTFIX 2: surfaceVariant set to Black to fix card background contrast with white text
val CyberpunkThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        // primaryContainer/secondaryContainer não eram definidos aqui, então o Material3
        // preenchia com o lavanda-padrão do baseline (não tem nada a ver com a identidade
        // preto+verde-neon do tema) sempre que algo (ex.: card "selecionado") usasse esses
        // tokens. Contraste conferido: onPrimaryContainer 9.46:1, onSecondaryContainer 13.71:1.
        primaryContainer = CyberMatrixDark,
        onPrimaryContainer = CyberMatrixGreen,
        secondary = CyberMatrixDark,
        secondaryContainer = Color(0xFF0A1F0A),
        onSecondaryContainer = Color(0xFF7CFF9B),
        tertiary = Color(0xFF00E5FF),
        // tertiaryContainer nunca tinha sido definido — caía no rosa-claro
        // padrão do Material3 bem no meio da estética preto+neon (ex.: card de
        // Vantagem "já selecionada" virava uma caixa rosa-pastel, e o texto
        // "Já selecionada" usava `tertiary` — o cyan claro do tema — que também
        // ficava ilegível em cima do próprio rosa claro, claro-sobre-claro).
        // Ciano escuro opaco, com o cyan do tema como "on" (mesmo padrão do
        // secondaryContainer/onSecondaryContainer verde acima). Contraste
        // onTertiaryContainer vs tertiaryContainer: 11.16:1.
        tertiaryContainer = Color(0xFF00272E),
        onTertiaryContainer = Color(0xFF7CE8FF),
        background = CyberMatrixBg,
        onBackground = CyberMatrixText,
        surface = Color(0xFF050505),
        onSurface = CyberMatrixText,
        surfaceVariant = Color(0xFF0E0E0E),
        onSurfaceVariant = Color(0xFF7CFF9B),
        surfaceContainer = Color(0xFF050505),
        surfaceContainerHigh = Color(0xFF0E0E0E), // Dialogs
        surfaceContainerLow = Color(0xFF000000),  // Cards
        surfaceContainerHighest = Color(0xFF141414), // Card de "requisito pendente"
        outline = CyberMatrixGreen.copy(alpha = 0.6f),
        outlineVariant = CyberMatrixGreen.copy(alpha = 0.35f),
        errorContainer = Color(0xFF3A0000),
        onErrorContainer = Color(0xFFFFCFCF)
    ),
    darkColors = darkColorScheme(
        primary = CyberMatrixGreen,
        onPrimary = Color.Black,
        primaryContainer = CyberMatrixDark,
        onPrimaryContainer = CyberMatrixGreen,
        secondary = CyberMatrixDark,
        secondaryContainer = Color(0xFF0A1F0A),
        onSecondaryContainer = Color(0xFF7CFF9B),
        tertiary = Color(0xFF00E5FF),
        tertiaryContainer = Color(0xFF00272E),
        onTertiaryContainer = Color(0xFF7CE8FF),
        background = Color(0xFF000000),
        onBackground = CyberMatrixText,
        surface = Color(0xFF060606),
        onSurface = CyberMatrixText,
        surfaceVariant = Color(0xFF141414),
        onSurfaceVariant = Color(0xFF7CFF9B),
        surfaceContainerHighest = Color(0xFF1A1A1A),
        outline = CyberMatrixGreen.copy(alpha = 0.6f),
        outlineVariant = CyberMatrixGreen.copy(alpha = 0.35f),
        errorContainer = Color(0xFF3A0000),
        onErrorContainer = Color(0xFFFFCFCF)
    ),
    typography = CyberpunkTypography,
    cardBorderColor = CyberMatrixGreen // Efeito Wireframe
)

// ─── Sci-Fi Theme ────────────────────────────────────────────────────────────
val SciFiThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        // Sem isso, primaryContainer/secondaryContainer caíam no lavanda-padrão do
        // Material3 em vez do azul holográfico do tema (contraste conferido: 10.62:1 / 8.32:1).
        primaryContainer = Color(0xFF0E3A44),
        onPrimaryContainer = Color(0xFFE6F0F4),
        secondary = Color(0xFF6FD8FF),
        onSecondary = Color(0xFF0C141C),
        secondaryContainer = Color(0xFF123240),
        onSecondaryContainer = Color(0xFF6FD8FF),
        tertiary = SciFiSilver,
        onTertiary = Color(0xFF0C141C),
        // tertiaryContainer nunca tinha sido definido — caía no rosa-claro
        // padrão do Material3 em vez do azul-metal do tema. Contraste
        // onTertiaryContainer vs tertiaryContainer: 10.46:1.
        tertiaryContainer = Color(0xFF1C2733),
        onTertiaryContainer = SciFiSilver,
        background = SciFiBlueDeep,
        surface = Color(0xFF0C131E),
        onBackground = SciFiSilver,
        onSurface = SciFiSilver,
        surfaceVariant = Color(0xFF131B26),
        onSurfaceVariant = Color(0xFFB0BEC5),
        surfaceContainer = Color(0xFF0C131E),
        surfaceContainerHigh = Color(0xFF131B26),
        surfaceContainerLow = Color(0xFF04070D),
        surfaceContainerHighest = Color(0xFF1C2733),
        outline = SciFiHolo.copy(alpha = 0.5f),
        outlineVariant = SciFiSilver.copy(alpha = 0.3f),
        errorContainer = Color(0xFF2A0E0E),
        onErrorContainer = Color(0xFFFFCDD2)
    ),
    darkColors = darkColorScheme(
        primary = SciFiHolo,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF0E3A44),
        onPrimaryContainer = Color(0xFFE6F0F4),
        secondary = Color(0xFF6FD8FF),
        onSecondary = Color(0xFF051018),
        secondaryContainer = Color(0xFF123240),
        onSecondaryContainer = Color(0xFF6FD8FF),
        tertiary = SciFiSilver,
        onTertiary = Color(0xFF051018),
        tertiaryContainer = Color(0xFF1C2733),
        onTertiaryContainer = SciFiSilver,
        background = Color(0xFF04070D),
        surface = Color(0xFF0A0F18),
        onBackground = Color(0xFFE6F0F4),
        onSurface = Color(0xFFE6F0F4),
        surfaceVariant = Color(0xFF151C27),
        onSurfaceVariant = Color(0xFFC0CCD2),
        surfaceContainerHighest = Color(0xFF202B38),
        outline = SciFiHolo.copy(alpha = 0.45f),
        outlineVariant = SciFiSilver.copy(alpha = 0.25f),
        errorContainer = Color(0xFF2A0E0E),
        onErrorContainer = Color(0xFFFFCDD2)
    ),
    typography = SciFiTypography,
    cardBorderColor = SciFiHolo.copy(alpha = 0.5f)
)

// ─── Horror Theme ────────────────────────────────────────────────────────────
// PASSO 4: Ajuste de contraste
val HorrorThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HorrorBlood, // Vermelho apenas para destaques
        onPrimary = Color.White,
        // Sem isso, primaryContainer/secondaryContainer caíam no lavanda-padrão do
        // Material3 em vez do vinho/sangue do tema (contraste conferido: 9.70:1 / 10.24:1).
        primaryContainer = Color(0xFF3A1212),
        onPrimaryContainer = HorrorBone,
        secondary = Color(0xFF4B1A1A),
        onSecondary = HorrorBone,
        secondaryContainer = Color(0xFF2A1414),
        onSecondaryContainer = HorrorBone,
        tertiary = Color(0xFF9E7B5B),
        onTertiary = Color(0xFF1A0F0F),
        // tertiaryContainer nunca tinha sido definido — caía no rosa-claro
        // padrão do Material3 em vez do castanho/osso do tema. Contraste
        // onTertiaryContainer vs tertiaryContainer: 11.10:1.
        tertiaryContainer = Color(0xFF2A1F14),
        onTertiaryContainer = Color(0xFFE6D4B8),
        background = HorrorBg,
        onBackground = HorrorBone,
        surface = Color(0xFF120B0B),
        onSurface = HorrorBone,
        surfaceVariant = Color(0xFF1E1212),
        onSurfaceVariant = Color(0xFFE6E0D4),
        surfaceContainer = Color(0xFF120B0B),
        surfaceContainerHigh = Color(0xFF1E1212),
        surfaceContainerLow = HorrorBg,
        surfaceContainerHighest = Color(0xFF2A1F14),
        outline = HorrorBlood.copy(alpha = 0.55f),
        outlineVariant = HorrorBlood.copy(alpha = 0.3f),
        errorContainer = Color(0xFF3A1212),
        onErrorContainer = HorrorBone
    ),
    darkColors = darkColorScheme(
        primary = HorrorBlood,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF3A1212),
        onPrimaryContainer = HorrorBone,
        secondary = Color(0xFF4B1A1A),
        onSecondary = HorrorBone,
        secondaryContainer = Color(0xFF2A1414),
        onSecondaryContainer = HorrorBone,
        tertiary = Color(0xFF9E7B5B),
        onTertiary = Color(0xFF1A0F0F),
        tertiaryContainer = Color(0xFF2A1F14),
        onTertiaryContainer = Color(0xFFE6D4B8),
        background = Color(0xFF050505),
        onBackground = Color(0xFFEBEBD3),
        surface = Color(0xFF0C0A0A),
        onSurface = Color(0xFFEBEBD3),
        surfaceVariant = Color(0xFF1B1212),
        onSurfaceVariant = Color(0xFFBDB7AA),
        surfaceContainerHighest = Color(0xFF241818),
        outline = HorrorBlood.copy(alpha = 0.5f),
        outlineVariant = HorrorBlood.copy(alpha = 0.28f),
        errorContainer = Color(0xFF3A1212),
        onErrorContainer = HorrorBone
    ),
    typography = HorrorTypography
)

// ─── WW2 Theme ───────────────────────────────────────────────────────────────
val WW2ThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = WW2Stamp,
        onPrimary = Color.White,
        // Opacos, não mais um overlay translúcido sobre o papel de telegrama
        // (mesma limpeza dos outros temas legados) — inclui trocar o
        // errorContainer, que usava WW2Olive (verde-oliva, sem nenhum tom de
        // alerta) por um vermelho-tijolo sóbrio, condizente com o resto do tema.
        primaryContainer = Color(0xFFD9CB8E),
        onPrimaryContainer = Color.Black,
        secondary = WW2Olive,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCFC976),
        onSecondaryContainer = Color.Black,
        tertiary = WW2Paper,
        onTertiary = Color.Black,
        // tertiaryContainer nunca tinha sido definido — caía no rosa-claro
        // padrão do Material3 em vez do khaki/papel do tema. Contraste
        // onTertiaryContainer vs tertiaryContainer: 16.04:1.
        tertiaryContainer = Color(0xFFEDE2A8),
        onTertiaryContainer = Color.Black,
        background = WW2Paper,
        surface = WW2Khaki,
        onSurface = Color.Black,
        onBackground = Color.Black,
        surfaceVariant = WW2Khaki.copy(alpha = 0.8f),
        onSurfaceVariant = Color.Black,
        surfaceContainerHighest = Color(0xFFE0D5A0),
        outline = WW2Stamp.copy(alpha = 0.4f),
        outlineVariant = WW2Stamp.copy(alpha = 0.25f),
        errorContainer = Color(0xFFE0B8A8),
        onErrorContainer = Color.Black
    ),
    darkColors = darkColorScheme(
        primary = WW2Paper,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF3D5133),
        onPrimaryContainer = WW2Paper,
        secondary = WW2Khaki,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF32433A),
        onSecondaryContainer = WW2Paper,
        tertiary = WW2Paper,
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFF4A4526),
        onTertiaryContainer = WW2Paper,
        background = WW2Olive,
        surface = Color(0xFF1B1D16),
        onSurface = WW2Khaki,
        // Khaki puro em cima do verde-oliva só fecha 3.89:1 de contraste (abaixo do
        // mínimo WCAG AA de 4.5:1 pra texto normal) — clareado pra 5.33:1 sem sair
        // da paleta khaki/militar do tema.
        onBackground = Color(0xFFDDCFAF),
        surfaceVariant = Color(0xFF2B2F25),
        onSurfaceVariant = WW2Khaki,
        surfaceContainerHighest = Color(0xFF383C2E),
        outline = WW2Paper.copy(alpha = 0.35f),
        outlineVariant = WW2Paper.copy(alpha = 0.2f),
        errorContainer = Color(0xFF2A1F00),
        onErrorContainer = WW2Khaki
    ),
    typography = WW2Typography
)

// ─── Halloween Theme ─────────────────────────────────────────────────────────
val HalloweenThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = HalloOrange,
        onPrimary = Color.Black,
        // Sem isso, primaryContainer/secondaryContainer caíam no lavanda-padrão do
        // Material3 em vez de abóbora/roxo (contraste conferido: 9.47:1 / 9.94:1).
        primaryContainer = Color(0xFF3A2200),
        onPrimaryContainer = Color(0xFFFFC37A),
        secondary = HalloPurple,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF241040),
        onSecondaryContainer = Color(0xFFD6B8FF),
        tertiary = Color(0xFFFFD54F),
        onTertiary = Color.Black,
        // tertiaryContainer nunca tinha sido definido — caía no rosa-claro
        // padrão do Material3 em vez do âmbar do tema. Contraste
        // onTertiaryContainer vs tertiaryContainer: 11.12:1.
        tertiaryContainer = Color(0xFF3A2E00),
        onTertiaryContainer = Color(0xFFFFE9A8),
        background = Color(0xFF0D0A0F),
        onBackground = Color(0xFFFFC37A),
        surface = Color(0xFF131017),
        onSurface = Color(0xFFFFC37A),
        surfaceVariant = Color(0xFF1C1624),
        onSurfaceVariant = Color(0xFFF3C48C),
        surfaceContainerHighest = Color(0xFF261E30),
        outline = HalloOrange.copy(alpha = 0.5f),
        outlineVariant = HalloPurple.copy(alpha = 0.35f),
        errorContainer = Color(0xFF2A0C0C),
        onErrorContainer = Color(0xFFFFC37A)
    ),
    darkColors = darkColorScheme(
        primary = HalloOrange,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF3A2200),
        onPrimaryContainer = Color(0xFFFFC37A),
        secondary = HalloPurple,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF241040),
        onSecondaryContainer = Color(0xFFD6B8FF),
        tertiary = Color(0xFFFFD54F),
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFF3A2E00),
        onTertiaryContainer = Color(0xFFFFE9A8),
        background = Color(0xFF060407),
        onBackground = Color(0xFFFFE0B2),
        surface = Color(0xFF0F0C12),
        onSurface = Color(0xFFFFE0B2),
        surfaceVariant = Color(0xFF1A1320),
        onSurfaceVariant = Color(0xFFE0C4A0),
        surfaceContainerHighest = Color(0xFF241A30),
        outline = HalloOrange.copy(alpha = 0.5f),
        outlineVariant = HalloPurple.copy(alpha = 0.35f),
        errorContainer = Color(0xFF2A0C0C),
        onErrorContainer = Color(0xFFFFE0B2)
    ),
    typography = HorrorTypography
)

// ─── Minimalist Theme ────────────────────────────────────────────────────────
val MinimalistThemeData = AppThemeData(
    lightColors = lightColorScheme(
        primary = MinimalistLightPrimary,
        onPrimary = Color.White,
        // Sem isso, primaryContainer/secondaryContainer caíam no lavanda-padrão do
        // Material3, destoando do monocromático do tema (contraste: 16.5:1 / 17.9:1).
        primaryContainer = Color(0xFFE4E4E4),
        onPrimaryContainer = Color.Black,
        secondary = Color(0xFF3D3D3D),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8E8E8),
        onSecondaryContainer = Color.Black,
        tertiary = MinimalistLightGreen,
        onTertiary = MinimalistLightText,
        // tertiaryContainer nunca tinha sido definido — caía no rosa-claro
        // padrão do Material3, destoando do monocromático (+ verde pálido)
        // do tema. Contraste onTertiaryContainer vs tertiaryContainer: 16.88:1.
        tertiaryContainer = Color(0xFFD7ECD8),
        onTertiaryContainer = Color.Black,
        background = MinimalistLightBg,
        surface = MinimalistLightSurface,
        onBackground = MinimalistLightText,
        onSurface = MinimalistLightText,
        surfaceVariant = Color(0xFFEDEDED),
        onSurfaceVariant = MinimalistLightText.copy(alpha = 0.85f),
        surfaceContainerHighest = Color(0xFFE0E0E0),
        outline = Color(0xFFBDBDBD),
        outlineVariant = Color(0xFFE0E0E0),
        errorContainer = MinimalistLightYellow,
        onErrorContainer = MinimalistLightText
    ),
    darkColors = darkColorScheme(
        primary = MinimalistDarkPrimary,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF333333),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFFD6D6D6),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF303030),
        onSecondaryContainer = Color.White,
        tertiary = MinimalistDarkGreen,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF1B4020),
        onTertiaryContainer = Color(0xFFB8E6BA),
        background = MinimalistDarkBg,
        surface = MinimalistDarkSurface,
        onBackground = MinimalistDarkText,
        onSurface = MinimalistDarkText,
        surfaceVariant = Color(0xFF2A2A2A),
        onSurfaceVariant = MinimalistDarkText.copy(alpha = 0.9f),
        surfaceContainerHighest = Color(0xFF383838),
        outline = Color(0xFF424242),
        outlineVariant = Color(0xFF333333),
        errorContainer = MinimalistDarkYellow,
        onErrorContainer = Color.Black
    ),
    typography = MinimalistTypography
)

val AllThemes = mapOf(
    AppTheme.DEFAULT to DefaultThemeData,
    AppTheme.MEDIEVAL to MedievalThemeData,
    AppTheme.CYBERPUNK to CyberpunkThemeData,
    AppTheme.WW2 to WW2ThemeData,
    AppTheme.HORROR to HorrorThemeData,
    AppTheme.SCIFI to SciFiThemeData,
    AppTheme.MINIMALIST to MinimalistThemeData,
    AppTheme.HALLOWEEN to HalloweenThemeData
)
