package com.example.angelitord.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angelitord.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastUpdated = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "DO"))
        .format(Date())

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Términos y Condiciones",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Última actualización: $lastUpdated",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            TermsSection(
                title = "1. Aceptación de los Términos",
                content = """
                    Al acceder y utilizar Angelito RD ("la Aplicación"), usted acepta estar sujeto a estos Términos y Condiciones. Si no está de acuerdo con alguna parte de estos términos, no debe utilizar la Aplicación.
                    
                    La Aplicación está diseñada para facilitar la organización de intercambios de regalos entre usuarios ("Angelitos"). El uso de esta aplicación es completamente voluntario y gratuito.
                """.trimIndent()
            )

            TermsSection(
                title = "2. Uso de la Aplicación",
                content = """
                    2.1. Debe tener al menos 13 años de edad para usar esta Aplicación.
                    
                    2.2. Es responsable de mantener la confidencialidad de su cuenta y contraseña.
                    
                    2.3. Se compromete a proporcionar información veraz y actualizada al registrarse.
                    
                    2.4. No debe usar la Aplicación para actividades ilegales o no autorizadas.
                    
                    2.5. No debe intentar obtener acceso no autorizado a cualquier parte de la Aplicación.
                """.trimIndent()
            )

            TermsSection(
                title = "3. Grupos y Sorteos",
                content = """
                    3.1. Puede crear grupos para organizar intercambios de regalos.
                    
                    3.2. Como administrador de un grupo, es responsable de la gestión del mismo.
                    
                    3.3. Los sorteos se realizan de forma aleatoria y automática.
                    
                    3.4. Una vez realizado un sorteo, no se puede deshacer automáticamente.
                    
                    3.5. La Aplicación no es responsable de los intercambios físicos de regalos entre usuarios.
                """.trimIndent()
            )

            TermsSection(
                title = "4. Privacidad y Datos",
                content = """
                    4.1. Recopilamos y procesamos sus datos según nuestra Política de Privacidad.
                    
                    4.2. Sus datos se almacenan de forma segura en servidores de Firebase.
                    
                    4.3. No compartimos sus datos personales con terceros sin su consentimiento.
                    
                    4.4. Puede solicitar la eliminación de sus datos en cualquier momento.
                """.trimIndent()
            )

            TermsSection(
                title = "5. Contenido del Usuario",
                content = """
                    5.1. Usted conserva todos los derechos sobre el contenido que publica.
                    
                    5.2. Al publicar contenido, nos otorga permiso para almacenarlo y mostrarlo.
                    
                    5.3. No debe publicar contenido ofensivo, ilegal o que infrinja derechos de terceros.
                    
                    5.4. Nos reservamos el derecho de eliminar contenido inapropiado.
                """.trimIndent()
            )

            TermsSection(
                title = "6. Limitación de Responsabilidad",
                content = """
                    6.1. La Aplicación se proporciona "tal cual" sin garantías de ningún tipo.
                    
                    6.2. No somos responsables de pérdidas o daños derivados del uso de la Aplicación.
                    
                    6.3. No garantizamos que la Aplicación esté libre de errores o interrupciones.
                    
                    6.4. No somos responsables de disputas entre usuarios respecto a los regalos.
                """.trimIndent()
            )

            TermsSection(
                title = "7. Modificaciones",
                content = """
                    7.1. Nos reservamos el derecho de modificar estos términos en cualquier momento.
                    
                    7.2. Las modificaciones entrarán en vigor al ser publicadas en la Aplicación.
                    
                    7.3. El uso continuado de la Aplicación constituye aceptación de los nuevos términos.
                """.trimIndent()
            )

            TermsSection(
                title = "8. Terminación",
                content = """
                    8.1. Puede dejar de usar la Aplicación en cualquier momento.
                    
                    8.2. Podemos suspender o terminar su acceso si viola estos términos.
                    
                    8.3. Puede solicitar la eliminación permanente de su cuenta desde la configuración.
                """.trimIndent()
            )

            TermsSection(
                title = "9. Ley Aplicable",
                content = """
                    Estos términos se rigen por las leyes de la República Dominicana. Cualquier disputa se resolverá en los tribunales competentes de Santo Domingo.
                """.trimIndent()
            )

            TermsSection(
                title = "10. Contacto",
                content = """
                    Si tiene preguntas sobre estos Términos y Condiciones, puede contactarnos a través de:
                    
                    Email: soporte@angelitord.com
                    
                    Dentro de la aplicación: Configuración > Ayuda y Soporte
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Al usar Angelito RD, usted acepta estos Términos y Condiciones.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TermsSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}