// Stripe Payment Processing
let stripe;
let elements;

// Initialize Stripe
document.addEventListener('DOMContentLoaded', async function() {
  // Get publishable key from backend
  const response = await fetch('/api/payment/config');
  const {publishableKey} = await response.json();
  stripe = Stripe(publishableKey);
});

// Initiate payment process
async function initiatePayment(planType, amount) {
  try {
    // 1. Create payment intent
    const response = await fetch('/api/payment/create-payment-intent', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        planType,
        amount,
      }),
    });
    
    const {clientSecret, error} = await response.json();
    
    if (error) {
      console.error('Payment initialization failed:', error);
      alert('Payment initialization failed: ' + error);
      return;
    }
    
    // 2. Show payment form
    showPaymentForm(clientSecret, planType, amount);
  } catch (err) {
    console.error('Error during payment process:', err);
    alert('An error occurred during payment. Please try again later.');
  }
}

// Show payment form
function showPaymentForm(clientSecret, planType, amount) {
  // Create modal to display payment form
  const modalHTML = `
    <div id="payment-modal" class="payment-modal">
      <div class="payment-modal-content">
        <span class="close-modal">&times;</span>
        <h2>Complete Your ${planType === 'monthly' ? 'Monthly' : 'Annual'} Subscription</h2>
        <p>Total amount: $${(amount/100).toFixed(2)}</p>
        
        <form id="payment-form">
          <div id="payment-element"></div>
          <button id="submit-payment">Pay Now</button>
          <div id="payment-message"></div>
        </form>
      </div>
    </div>
  `;
  
  // Add modal to page
  const modalContainer = document.createElement('div');
  modalContainer.innerHTML = modalHTML;
  document.body.appendChild(modalContainer);
  
  // Get DOM elements
  const modal = document.getElementById('payment-modal');
  const closeBtn = document.querySelector('.close-modal');
  const form = document.getElementById('payment-form');
  const submitBtn = document.getElementById('submit-payment');
  const messageDiv = document.getElementById('payment-message');
  
  // Show modal
  modal.style.display = 'block';
  
  // Close modal
  closeBtn.onclick = function() {
    modal.style.display = 'none';
    setTimeout(() => modal.remove(), 300);
  };
  
  // Close when clicking outside modal
  window.onclick = function(event) {
    if (event.target === modal) {
      modal.style.display = 'none';
      setTimeout(() => modal.remove(), 300);
    }
  };
  
  // Create payment element
  elements = stripe.elements({clientSecret});
  const paymentElement = elements.create('payment');
  paymentElement.mount('#payment-element');
  
  // Handle form submission
  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    
    // Disable submit button to prevent multiple submissions
    submitBtn.disabled = true;
    submitBtn.textContent = 'Processing...';
    
    // Confirm payment
    const {error} = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/payment-success.html?plan=${planType}`,
      },
    });
    
    if (error) {
      messageDiv.textContent = error.message;
      submitBtn.disabled = false;
      submitBtn.textContent = 'Pay Now';
    }
  });
}

// Add CSS styles
const style = document.createElement('style');
style.textContent = `
.payment-modal {
  display: none;
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
}

.payment-modal-content {
  background-color: #fff;
  margin: 10% auto;
  padding: 30px;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}

.close-modal {
  float: right;
  font-size: 28px;
  cursor: pointer;
}

#payment-form {
  margin-top: 20px;
}

#payment-element {
  margin-bottom: 24px;
}

#submit-payment {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  width: 100%;
  transition: background-color 0.3s;
}

#submit-payment:hover {
  background-color: #2980b9;
}

#submit-payment:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

#payment-message {
  color: #e74c3c;
  margin-top: 12px;
  text-align: center;
}
`;
document.head.appendChild(style); 