package com.example.angelitord.utils


import kotlin.random.Random

/**
 * Algoritmo para asignar angelitos de forma aleatoria
 * Garantiza que nadie se toque a sí mismo y que sea un ciclo cerrado
 */
object AngelitoAssigner {
    /**
     * Genera asignaciones aleatorias de angelitos
     * @param memberIds Lista de IDs de participantes (mínimo 3)
     * @return Map donde la key es el "dador" y el value es el "receptor"
     * @throws IllegalArgumentException si hay menos de 3 participantes
     */
    fun assignAngelitos(memberIds: List<String>): Map<String, String> {
        require(memberIds.size >= 3) {
            "Se necesitan al menos 3 participantes para crear un grupo de angelito"
        }

        // Crear una permutación válida usando el algoritmo de ciclo aleatorio
        val receivers = memberIds.toMutableList()
        var isValid = false
        var attempts = 0
        val maxAttempts = 100

        while (!isValid && attempts < maxAttempts) {
            receivers.shuffle(Random.Default)
            isValid = checkValidAssignment(memberIds, receivers)
            attempts++
        }

        // Si después de varios intentos no se logra, usar algoritmo determinista
        if (!isValid) {
            return generateDeterministicAssignment(memberIds)
        }

        return memberIds.zip(receivers).toMap()
    }

    /**
     * Verifica que ninguna persona se tenga a sí misma
     */
    private fun checkValidAssignment(givers: List<String>, receivers: List<String>): Boolean {
        return givers.indices.none { i -> givers[i] == receivers[i] }
    }

    /**
     * Genera una asignación determinista válida usando rotación
     * Este método garantiza que siempre habrá una solución válida
     */
    private fun generateDeterministicAssignment(memberIds: List<String>): Map<String, String> {
        val shuffled = memberIds.shuffled()
        val assignments = mutableMapOf<String, String>()

        for (i in shuffled.indices) {
            val nextIndex = (i + 1) % shuffled.size
            assignments[shuffled[i]] = shuffled[nextIndex]
        }

        return assignments
    }

    /**
     * Verifica que el ciclo sea cerrado (todos dan y reciben)
     */
    fun validateAssignments(assignments: Map<String, String>): Boolean {
        val givers = assignments.keys
        val receivers = assignments.values.toSet()

        // Todos deben dar y recibir
        if (givers != receivers) return false

        // Nadie debe tenerse a sí mismo
        if (assignments.any { (giver, receiver) -> giver == receiver }) return false

        return true
    }
}