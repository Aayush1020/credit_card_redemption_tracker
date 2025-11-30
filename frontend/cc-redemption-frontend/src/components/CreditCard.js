import React, { useState } from 'react';
import RedemptionItem from './RedemptionItem';

function CreditCard({ card, onAddRedemption, onToggleRedemption, onDeleteRedemption, onDeleteCard }) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [newRedemptionName, setNewRedemptionName] = useState('');
  const [newRedemptionFrequency, setNewRedemptionFrequency] = useState('MONTHLY');
  const [newRedemptionAmount, setNewRedemptionAmount] = useState(0);

  const handleAddRedemption = () => {
    if (newRedemptionName.trim()) {
      onAddRedemption(card.id, { name: newRedemptionName, frequency: newRedemptionFrequency, checked: false, amount: parseFloat(newRedemptionAmount) });
      setNewRedemptionName('');
      setNewRedemptionAmount(0);
    }
  };

  return (
    <div className="credit-card">
      <h2 onClick={() => setIsExpanded(!isExpanded)}>
        {card.name} ({card.description})
        <button onClick={() => onDeleteCard(card.id)}>Delete Card</button>
      </h2>
      {isExpanded && (
        <div className="redemptions-list">
          <h3>Redemptions:</h3>
          {card.redemptions && card.redemptions.map(redemption => (
            <RedemptionItem
              key={redemption.id}
              redemption={redemption}
              onToggleChecked={(redemptionId) => onToggleRedemption(card.id, redemptionId)}
              onDelete={(redemptionId) => onDeleteRedemption(card.id, redemptionId)}
            />
          ))}
          <div className="add-redemption">
            <input
              type="text"
              placeholder="Redemption Name"
              value={newRedemptionName}
              onChange={(e) => setNewRedemptionName(e.target.value)}
            />
            <input
              type="number"
              placeholder="Amount"
              value={newRedemptionAmount}
              onChange={(e) => setNewRedemptionAmount(e.target.value)}
            />
            <select
              value={newRedemptionFrequency}
              onChange={(e) => setNewRedemptionFrequency(e.target.value)}
            >
              <option value="MONTHLY">Monthly</option>
              <option value="QUARTERLY">Quarterly</option>
              <option value="BIANNUAL">Biannual</option>
              <option value="YEARLY">Yearly</option>
            </select>
            <button onClick={handleAddRedemption}>Add Redemption</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default CreditCard;