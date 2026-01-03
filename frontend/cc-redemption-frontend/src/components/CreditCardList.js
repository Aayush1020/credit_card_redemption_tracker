import React, { useState, useEffect } from 'react';
import CreditCard from './CreditCard';

function CreditCardList() {
  const [creditCards, setCreditCards] = useState([]);
  const [newCardName, setNewCardName] = useState('');
  const [newCardDescription, setNewCardDescription] = useState('');
  const [currentYearReimbursementTotal, setCurrentYearReimbursementTotal] = useState(0);

  const API_BASE_URL = 'http://localhost:8080/api/creditcards';

  const fetchCreditCards = async () => {
    try {
      const response = await fetch(API_BASE_URL);
      const data = await response.json();
      setCreditCards(data);
    //   console.log("Fetched credit cards:", data);
    } catch (error) {
      console.error("Error fetching credit cards:", error);
    }
  };

  const fetchCurrentYearReimbursementTotal = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/reimbursements/current-year-total`);
      const total = await response.json();
      setCurrentYearReimbursementTotal(total);
    } catch (error) {
      console.error("Error fetching current year reimbursement total:", error);
    }
  };

  useEffect(() => {
    fetchCreditCards();
    fetchCurrentYearReimbursementTotal();
  }, []);

  const handleAddCard = async () => {
    if (newCardName.trim()) {
      const newCard = {
        name: newCardName,
        description: newCardDescription,
        redemptions: [],
      };
      try {
        const response = await fetch(API_BASE_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(newCard),
        });
        const savedCard = await response.json();
        setCreditCards([...creditCards, savedCard]);
        setNewCardName('');
        setNewCardDescription('');
        fetchCurrentYearReimbursementTotal(); // Re-fetch total after adding card
      } catch (error) {
        console.error("Error adding credit card:", error);
      }
    }
  };

  const handleDeleteCard = async (id) => {
    try {
      await fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE',
      });
      setCreditCards(creditCards.filter(card => card.id !== id));
      fetchCurrentYearReimbursementTotal(); // Re-fetch total after deleting card
    } catch (error) {
      console.error("Error deleting credit card:", error);
    }
  };

  const handleAddRedemption = async (cardId, redemption) => {
    try {
      const response = await fetch(`${API_BASE_URL}/${cardId}/redemptions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(redemption),
      });
      const savedRedemption = await response.json();
      setCreditCards(creditCards.map(card =>
        card.id === cardId
          ? { ...card, redemptions: [...card.redemptions, savedRedemption] }
          : card
      ));
      fetchCurrentYearReimbursementTotal(); // Re-fetch total after adding redemption
    } catch (error) {
      console.error("Error adding redemption:", error);
    }
  };

  const handleToggleRedemption = async (cardId, redemptionId) => {
    // console.log("Attempting to toggle redemption for cardId:", cardId, "redemptionId:", redemptionId);
    // console.log("Current creditCards state:", creditCards);
    const cardToUpdate = creditCards.find(card => card.id === cardId);
    if (!cardToUpdate) {
    //   console.error("Credit card not found for toggling redemption:", cardId);
      return;
    }

    const redemptionToUpdate = cardToUpdate.redemptions.find(red => red.id === redemptionId);
    if (!redemptionToUpdate) {
    //   console.error("Redemption not found for toggling:", redemptionId);
      return;
    }
    const newCheckedStatus = !redemptionToUpdate.checked;

    try {
      const response = await fetch(`${API_BASE_URL}/${cardId}/redemptions/${redemptionId}/checked?checked=${newCheckedStatus}`, {
   method: 'PUT',
      });
      const updatedRedemption = await response.json();
      setCreditCards(creditCards.map(card =>
        card.id === cardId
          ? { ...card,
              redemptions: card.redemptions.map(redemption =>
                redemption.id === redemptionId ? updatedRedemption : redemption
              ),
            }
          : card
      ));
      fetchCurrentYearReimbursementTotal(); // Re-fetch total after toggling redemption
      fetchCreditCards(); // Re-fetch all credit cards to update per-card totals
    } catch (error) {
      console.error("Error updating redemption status:", error);
    }
  };

  const handleDeleteRedemption = async (cardId, redemptionId) => {
    // console.log("Attempting to delete redemption for cardId:", cardId, "redemptionId:", redemptionId);
    // console.log("Current creditCards state:", creditCards);
    const cardToUpdate = creditCards.find(card => card.id === cardId);
    if (!cardToUpdate) {
    //   console.error("Credit card not found for deleting redemption:", cardId);
      return;
    }

    const redemptionToUpdate = cardToUpdate.redemptions.find(red => red.id === redemptionId);
    if (!redemptionToUpdate) {
    //   console.error("Redemption not found for deleting:", redemptionId);
      return;
    }
    try {
      await fetch(`${API_BASE_URL}/${cardId}/redemptions/${redemptionId}`, {
        method: 'DELETE',
      });
      setCreditCards(creditCards.map(card =>
        card.id === cardId
          ? { ...card, redemptions: card.redemptions.filter(redemption => redemption.id !== redemptionId) }
          : card
      ));
      fetchCurrentYearReimbursementTotal(); // Re-fetch total after deleting redemption
    } catch (error) {
      console.error("Error deleting redemption:", error);
    }
  };

  return (
    <div className="credit-card-list">
      <div className="total-reimbursements">
        <h3>Total Reimbursements This Year: ${currentYearReimbursementTotal.toFixed(2)}</h3>
      </div>
      <div className="add-card">
        <input
          type="text"
          placeholder="Credit Card Name"
          value={newCardName}
          onChange={(e) => setNewCardName(e.target.value)}
        />
        <input
          type="text"
          placeholder="Description (Optional)"
          value={newCardDescription}
          onChange={(e) => setNewCardDescription(e.target.value)}
        />
        <button onClick={handleAddCard}>Add Credit Card</button>
      </div>
      {creditCards.map(card => (
        <CreditCard
          key={card.id}
          card={card}
          onAddRedemption={handleAddRedemption}
          onToggleRedemption={handleToggleRedemption}
          onDeleteRedemption={handleDeleteRedemption}
          onDeleteCard={handleDeleteCard}
        />
      ))}
    </div>
  );
}

export default CreditCardList;