import { AdditionalActionMode } from "./additional-action-mode";

export interface AdditionalActionBase {
  icon?: string;
  messageKey: string;
  buttonClass?: string;
}

/**
 * An additional global action (does not operate on a specific entity)
 */
export interface AdditionalGlobalAction extends AdditionalActionBase {
  action: () => void;
  enabled?: () => boolean;
}

/**
 * An additional action that operates on a table row
 */
export interface AdditionalRowAction extends AdditionalActionBase {
  action: (obj: any) => void;
  enabled?: (obj: any) => boolean;
}

/**
 * An additional action that operates on a form
 */
export interface AdditionalFormAction extends AdditionalRowAction {
  mode: AdditionalActionMode;
}
