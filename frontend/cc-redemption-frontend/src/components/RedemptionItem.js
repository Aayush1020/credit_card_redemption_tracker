import React from 'react';

function RedemptionItem({ redemption, onToggleChecked, onDelete }) {
  return (
    <div className="redemption-item">
      <input
        type="checkbox"
        checked={redemption.checked}
        onChange={() => onToggleChecked(redemption.id)}
      />
      <span>{redemption.name} - ${redemption.amount} ({redemption.frequency})</span>
      <button onClick={() => onDelete(redemption.id)}>Delete</button>
    </div>
  );
}

export default RedemptionItem;