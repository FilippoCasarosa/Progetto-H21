/**
 * Modello per la gestione dei tipi di componenti disponibili
 * @author Filippo Casarosa
 */

export interface ComponentType {
  id: number;
  name: string;
  sortOrder: number;
  active: boolean;
}
